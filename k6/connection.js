import ws from 'k6/ws';
import { check, sleep, fail } from 'k6';
import http from 'k6/http';
import { Counter, Rate, Trend, Gauge } from 'k6/metrics';
import exec from 'k6/execution';

// --- 설정 ---
const API_SERVER_URL = 'http://01.taehyeongkim.shop';
const CHAT_SERVER_STOMP_URL = 'ws://01.taehyeongkim.shop/ws';
const MAX_CONNECTIONS_TARGET = 20000; // 테스트할 총 WebSocket 연결 수
const RAMP_UP_DURATION = '30m';
const SUSTAIN_DURATION = '5m';
const RAMP_DOWN_DURATION = '1m';
const SESSION_COOKIE_NAME = 'JSESSIONID';
const STOMP_HEARTBEAT_CLIENT_CX = 10000;
const STOMP_HEARTBEAT_CLIENT_CY = 10000;
const SINGLE_LOGIN_USERNAME = 'test1'; // 단일 로그인에 사용할 사용자 이름
const SINGLE_LOGIN_PASSWORD = 'test1234'; // 단일 로그인에 사용할 비밀번호
const SETUP_TIMEOUT = '5m'; // 단일 로그인만 하므로 setup 시간 단축

// --- 메트릭 정의 ---
const successfulLogins = new Counter('successful_logins'); // 이제 1번만 성공해야 함
const failedLogins = new Counter('failed_logins');
const successfulStompConnections = new Counter('successful_stomp_connections');
const failedStompConnections = new Counter('failed_stomp_connections');
const activeStompConnections = new Gauge('active_stomp_connections');
const unexpectedStompDisconnects = new Counter('unexpected_stomp_disconnects');
const errorRate = new Rate('errors');
const stompConnectionTime = new Trend('stomp_connection_time', true);

// --- 테스트 옵션 ---
export const options = {
    scenarios: {
        stomp_connection_stress: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: RAMP_UP_DURATION, target: MAX_CONNECTIONS_TARGET }, // VU 수는 연결 수와 동일하게
                { duration: SUSTAIN_DURATION, target: MAX_CONNECTIONS_TARGET },
            ],
            gracefulRampDown: RAMP_DOWN_DURATION,
            exec: 'maintainStompConnection',
        },
    },
    thresholds: {
        'failed_logins': ['count == 0'], // 단일 로그인이므로 실패하면 안 됨
        'successful_logins': ['count == 1'], // 단일 로그인 성공 확인
        'failed_stomp_connections': [`count < ${MAX_CONNECTIONS_TARGET * 0.05}`],
        'stomp_connection_time{scenario:stomp_connection_stress}': ['p(95) < 10000'],
        'unexpected_stomp_disconnects': [`count < ${MAX_CONNECTIONS_TARGET * 0.02}`],
        'errors': ['rate < 0.1'],
        'http_req_failed{scenario:setup_login}': ['rate == 0'], // 단일 로그인 요청 실패율 0
    },
    setupTimeout: SETUP_TIMEOUT,
};

// --- Setup 함수 (단일 사용자 로그인) ---
export function setup() {
    console.log(`[Setup] Logging in with a single user: ${SINGLE_LOGIN_USERNAME}`);
    let singleUserCookie = null;

    const loginPayload = JSON.stringify({ username: SINGLE_LOGIN_USERNAME, password: SINGLE_LOGIN_PASSWORD });

    const loginRes = http.post(
        `${API_SERVER_URL}/api/auth/login`,
        loginPayload,
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { scenario: 'setup_login', name: 'SetupSingleUserLogin' },
            // jar: null, // k6 기본 쿠키 Jar 사용 방지, 응답 헤더에서 직접 파싱
        }
    );

    if (loginRes && loginRes.status === 200) {
        let jsessionIdValue = null;
        const setCookieHeaderValue = loginRes.headers['Set-Cookie'] || loginRes.headers['set-cookie'];

        if (setCookieHeaderValue) {
            const cookiesToParse = Array.isArray(setCookieHeaderValue) ? setCookieHeaderValue[0] : setCookieHeaderValue;
            if (typeof cookiesToParse === 'string') {
                const match = cookiesToParse.match(new RegExp(`${SESSION_COOKIE_NAME}=([^;]+)`));
                if (match && match[1]) {
                    jsessionIdValue = match[1];
                }
            }
        }

        if (jsessionIdValue) {
            singleUserCookie = `${SESSION_COOKIE_NAME}=${jsessionIdValue}`;
            successfulLogins.add(1);
            console.log(`[Setup] Single user login successful. Cookie: ${singleUserCookie}`);
        } else {
            console.error(`[Setup] Single user login OK but FAILED TO PARSE '${SESSION_COOKIE_NAME}' from Set-Cookie header. Headers: ${JSON.stringify(loginRes.headers)}, Body: ${loginRes.body}`);
            failedLogins.add(1);
            errorRate.add(1, { tag: 'setup_single_user_cookie_parsing_fail' });
            fail('Failed to get session cookie for single user in setup.');
        }
    } else {
        const status = loginRes ? loginRes.status : 'N/A';
        const body = loginRes ? loginRes.body : 'N/A';
        console.error(`[Setup] Single user login failed. Status: ${status}, Body: ${body}`);
        failedLogins.add(1);
        errorRate.add(1, { tag: 'setup_single_user_login_fail', status_code: String(status) });
        fail('Single user login failed in setup.');
    }

    // 모든 VU가 이 단일 쿠키를 사용하도록 반환
    return { singleUserCookie };
}


// --- STOMP 연결 유지 테스트 함수 (단일 사용자의 쿠키 사용) ---
export function maintainStompConnection(data) {
    const vuId = exec.vu.idInTest; // 각 VU는 고유 ID를 가짐
    const scenario = exec.scenario;

    if (!data.singleUserCookie) { // setup에서 전달된 단일 쿠키 확인
        console.error(`[VU ${vuId}] No single user cookie provided from setup. Skipping STOMP.`);
        errorRate.add(1, { tag: 'no_single_user_cookie' });
        failedStompConnections.add(1); // 연결 시도조차 못하므로 실패로 간주
        return;
    }
    const cookieHeaderValue = data.singleUserCookie; // 모든 VU가 동일한 쿠키 사용

    const url = CHAT_SERVER_STOMP_URL;
    const params = {
        headers: { 'Cookie': cookieHeaderValue },
        tags: { stomp_user: SINGLE_LOGIN_USERNAME, vu_id: vuId, scenario_name: scenario.name } // 태그에 VU ID 추가하여 구분
    };

    let isStompActuallyConnected = false;
    const connAttemptStart = Date.now();

    const res = ws.connect(url, params, function (socket) {
        let heartbeatIntervalId = null;

        socket.on('open', () => {
            const connectFrame = `CONNECT\naccept-version:1.2,1.1,1.0\nheart-beat:${STOMP_HEARTBEAT_CLIENT_CX},${STOMP_HEARTBEAT_CLIENT_CY}\nhost:localhost\n\n\x00`;
            try {
                socket.send(connectFrame);
            } catch (e) {
                console.error(`[VU ${vuId} using ${SINGLE_LOGIN_USERNAME}] Error sending STOMP CONNECT frame: ${e}.`);
                failedStompConnections.add(1);
                errorRate.add(1, { tag: 'stomp_connect_send_fail' });
                return;
            }
        });

        socket.on('message', (stompMessage) => {
            const firstLine = stompMessage.substring(0, stompMessage.indexOf('\n')).trim();

            if (firstLine === 'CONNECTED') {
                if (!isStompActuallyConnected) {
                    isStompActuallyConnected = true;
                    successfulStompConnections.add(1);
                    activeStompConnections.add(1);
                }
                stompConnectionTime.add(Date.now() - connAttemptStart, { scenario_name: scenario.name });

                let serverWantsClientToSendInterval = STOMP_HEARTBEAT_CLIENT_CX;
                const heartBeatHeader = stompMessage.match(/heart-beat:(\d+),(\d+)/);
                if (heartBeatHeader && heartBeatHeader[1] && parseInt(heartBeatHeader[1], 10) > 0) {
                    serverWantsClientToSendInterval = parseInt(heartBeatHeader[1], 10);
                }

                if (serverWantsClientToSendInterval > 0) {
                    if (heartbeatIntervalId) socket.clearInterval(heartbeatIntervalId);
                    heartbeatIntervalId = socket.setInterval(() => {
                        if (socket.readyState === ws.OPEN) {
                            try { socket.send('\n'); }
                            catch (e) {
                                console.warn(`[VU ${vuId} using ${SINGLE_LOGIN_USERNAME}] Error sending STOMP heartbeat: ${e}. Clearing interval.`);
                                if (heartbeatIntervalId) socket.clearInterval(heartbeatIntervalId);
                            }
                        } else {
                            if (heartbeatIntervalId) socket.clearInterval(heartbeatIntervalId);
                        }
                    }, serverWantsClientToSendInterval * 0.8);
                }
            } else if (firstLine === 'ERROR') {
                console.error(`[VU ${vuId} using ${SINGLE_LOGIN_USERNAME}] STOMP ERROR frame: ${stompMessage}`);
                errorRate.add(1, { tag: 'stomp_error_frame' });
                if (isStompActuallyConnected) {
                    activeStompConnections.add(-1);
                    isStompActuallyConnected = false;
                }
                failedStompConnections.add(1);
                if (heartbeatIntervalId) socket.clearInterval(heartbeatIntervalId);
            }
        });

        socket.on('error', (e) => {
            console.error(`[VU ${vuId} using ${SINGLE_LOGIN_USERNAME}] WebSocket layer error: ${e.error()}`);
            errorRate.add(1, { tag: 'websocket_layer_error' });
            if (isStompActuallyConnected) {
                activeStompConnections.add(-1);
                isStompActuallyConnected = false;
                unexpectedStompDisconnects.add(1);
            } else {
                failedStompConnections.add(1);
            }
            if (heartbeatIntervalId) socket.clearInterval(heartbeatIntervalId);
        });

        socket.on('close', (code) => {
            if (heartbeatIntervalId) socket.clearInterval(heartbeatIntervalId);
            if (isStompActuallyConnected) {
                activeStompConnections.add(-1);
                isStompActuallyConnected = false;
                if (code !== 1000 && code !== 1001) {
                    unexpectedStompDisconnects.add(1);
                    errorRate.add(1, { tag: `unexpected_ws_close_code_${code}` });
                }
            }
        });
    });

    check(res, {
        'WebSocket handshake initiated successfully': (s) => s && (s.readyState === ws.OPEN || s.readyState === ws.CONNECTING),
    }, { scenario_name: scenario.name, ws_conn_attempt: 'initial_check', user: SINGLE_LOGIN_USERNAME, vu_id: vuId }) || (() => {
        stompConnectionTime.add(Date.now() - connAttemptStart, { scenario_name: scenario.name });
        console.error(`[VU ${vuId} using ${SINGLE_LOGIN_USERNAME}] WebSocket connection attempt FAILED immediately or socket is null. Response: ${JSON.stringify(res)}`);
        failedStompConnections.add(1);
        errorRate.add(1, { tag: 'ws_handshake_failed_immediate' });
    })();
}

// --- Teardown 함수 ---
export function teardown(data) {
    console.log(`\n--- STOMP Connection Stress Test (Single User Session) FINAL SUMMARY ---`);
    console.log(`Target Max Connections Configured: ${MAX_CONNECTIONS_TARGET}`);
    if (data && data.singleUserCookie) { // singleUserCookie 존재 여부로 setup 성공 확인
        console.log(`Single User Login Cookie Prepared: Yes`);
    } else {
        console.log(`Single User Login Cookie Prepared: No (Setup likely failed)`);
    }
    console.log(`- Successful Logins (Setup): ${successfulLogins.count}`); // 항상 1이어야 함
    console.log(`- Failed Logins (Setup): ${failedLogins.count}`);       // 항상 0이어야 함
    console.log(`Total Successful STOMP Connections (CONNECTED frame): ${successfulStompConnections.count}`);
    console.log(`Total Failed STOMP Connections (CONNECT fail/ERROR frame/WS fail): ${failedStompConnections.count}`);
    console.log(`Total Unexpected STOMP/WS Disconnects (while STOMP was active): ${unexpectedStompDisconnects.count}`);
    console.log(`---> For MAX concurrently active STOMP connections, check 'active_stomp_connections' GAUGE in k6 output.`);
    console.log(`---> For STOMP connection time distribution, check 'stomp_connection_time' TREND.`);
    console.log(`---> For errors, check 'errors' RATE and specific failure counters.`);
}