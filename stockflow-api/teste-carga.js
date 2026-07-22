import http from 'k6/http';
import { check, sleep } from 'k6';

const TOKEN = 'eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJObUJLZTRwWm1vd01XY2lNakc4Ym91ZHJ5SnJwZ0Q1elFmWkpPdjJTXzVjIn0.eyJleHAiOjE3ODQ3NDQ5MjIsImlhdCI6MTc4NDc0NDYyMiwiYXV0aF90aW1lIjoxNzg0NzQzMzY0LCJqdGkiOiIyOGVmZjJjOC1jOWEzLTRlZmMtYWJjYS0yNDNhOTgwNDA4MDkiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjYwNjIvcmVhbG1zL3N0b2NrLWZsb3ctcmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOGY0ZGNlYzgtYmZjOS00ZGEwLTk5YzgtOTEyMTA2NGY2ZTRmIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3RvY2stZmxvdy1hcHAiLCJzaWQiOiI2Y2JiNDYyNS1iYTM3LTRiYzItOTk1YS0wYmVkZWE5MTA5YWUiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6NDIwMCJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1zdG9jay1mbG93LXJlYWxtIiwib2ZmbGluZV9hY2Nlc3MiLCJBRE1JTiIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6Ikd1dGkgWmljYSIsInByZWZlcnJlZF91c2VybmFtZSI6Imd1dGl6aWNhIiwiZ2l2ZW5fbmFtZSI6Ikd1dGkiLCJmYW1pbHlfbmFtZSI6IlppY2EiLCJlbWFpbCI6Imd1dGl6aWNhQGdtYWlsLmNvbSJ9.bjdT1SGIoVAOSdrvEcHuh1d-S_6GPWhqKhHNSxICxBVaaOV940VoNSWhZZfgw8ICyXfbmr_-312dmZaOpyGWwBfPIgi3UZBBKmsupFKa-i4EOzHeOtslwc2dGVDBaWzArzF9V0QRJF4Qh5sY2vhxLNEShVX0Ve1OXuu8ifTsiZbVkyFabtPl9Mq_SN5vCA1SrZQVFrTvS3rBv_1nST-y0qsmBnl8i7b7SzcT_Ygm8Pe6E7sXBG2o1RkiOr8phznXDFiHH8oSWudul346oQa9a786JclKRctZ_0KNSjhrSej9YGhYMlTi7t1V2--Grhak0NHMIlpZYpnAOuAst2kJCg';

export const options = {
    vus: 50,
    duration: '30s',
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

export default function () {
    const params = {
        headers: {
            'Authorization': `Bearer ${TOKEN}`,
            'Content-Type': 'application/json'
        },
    };

    const res = http.get('http://stockflow-api:6060/api/v1/products', params);

    check(res, {
        'status é 200 (Autenticado e com dados)': (r) => r.status === 200,
        'não tomou 401/403': (r) => r.status !== 401 && r.status !== 403,
    });

    sleep(0.01);
}