import { expect } from 'chai';
import axios from 'axios';

const SERVER1_URL = 'http://localhost:8197';

const authConfig = {
    auth: {
        username: 'admin',
        password: 'admin',
    },
};

const authenticatedAxios = axios.create(authConfig);

describe('Server API Tests', function () {
    this.timeout(5000); // Set timeout to 5 seconds

    it('should return information from the root endpoint', async () => {
        const response = await axios.get(`${SERVER1_URL}/request`);
        expect(response.status).to.equal(200);
        expect(response.data).to.have.property('Service');
        expect(response.data).to.have.property('Service2');
    });

    it('should get the current state', async () => {
        const response = await axios.get(`${SERVER1_URL}/state`);
        expect(response.status).to.equal(200);
        expect(['INIT', 'PAUSED', 'RUNNING', 'SHUTDOWN']).to.include(response.data);
    });

    it('should not allow state change without login (401)', async () => {
        for (const state of ['PAUSED', 'RUNNING']) {
            try {
                await axios.put(`${SERVER1_URL}/state`, null, {
                    params: { state },
                });
            } catch (error) {
                expect(error.response.status).to.equal(401);
                // Instead check if the HTML error page contains the keyword Unauthorized
                expect(error.response.data).to.include('401');
            }
        }
    });

    it('should allow state change after login as admin', async () => {
        for (const state of ['PAUSED', 'RUNNING']) {
            const response = await authenticatedAxios.put(`${SERVER1_URL}/state`, null, {
                params: { state },
            });
            expect(response.status).to.equal(200);
            expect(response.data).to.equal(state);
        }
    });

    it('should return the run log', async () => {
        const response = await axios.get(`${SERVER1_URL}/run-log`);
        expect(response.status).to.equal(200);
        expect(response.data).to.be.a('string');
    });

    it('should stop the service', async () => {
        try {
            const response = await authenticatedAxios.get(`${SERVER1_URL}/stop`);
            expect(response.status).to.equal(200);
            expect(response.data).to.include('All services are stopping...');
        } catch (error) {
            if (error.response && error.response.status === 500) {
                expect(error.response.data).to.include('Error stopping services');
            } else {
                throw error;
            }
        }
    });
});
