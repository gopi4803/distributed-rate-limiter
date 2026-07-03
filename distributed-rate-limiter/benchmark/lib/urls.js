import { config } from './config.js';

export const urls = {

    benchmark() {
        return `${config.baseUrl}${config.endpoint}`;
    }

};