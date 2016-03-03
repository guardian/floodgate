import Reqwest from 'reqwest';

export default {

    getContentSources:() => {
        return Reqwest({
            url: '/content-source',
            contentType: 'text/json',
            method: 'get'
        })
    },

    getContentSourcesWithId:(id) => {
        return Reqwest({
            url: '/content-source/' + id,
            contentType: 'text/json',
            method: 'get'
        })
    },

    createContentSource:(form) => {
        return Reqwest({
            url: '/content-source',
            contentType: 'text/json',
            method: 'post',
            data: JSON.stringify(form)
        })
    },

    updateContentSource:(id, environment, form) => {
        return Reqwest({
            url: '/content-source/' + id + '/' + environment,
            contentType: 'text/json',
            method: 'put',
            data: JSON.stringify(form)
        })
    },

    getReindexHistory:(id, environment) => {
        return Reqwest({
            url: '/content-source/' + id + '/' + environment + '/reindex/history',
            contentType: 'text/json',
            method: 'get'
        })
    },

    getRunningReindex:(id, environment) => {
        return Reqwest({
            url: '/content-source/' + id + '/' + environment + '/reindex/running',
            contentType: 'text/json',
            method: 'get'
        })
    },

    initiateReindex:(id, environment, startDate, endDate) => {
        var url = '/content-source/' + id + '/' + environment + '/reindex';
        if(startDate != '' && endDate === '') url += '?from=' + startDate;
        else if(startDate === '' && endDate != '') url += '?to=' + endDate;
        else if(startDate != '' && endDate != '') url += '?from=' + startDate + "&to=" + endDate;

        return Reqwest({
            url: url,
            method: 'post'
        })
    },

    cancelReindex:(id, environment) => {
        return Reqwest({
            url: '/content-source/' + id + '/' + environment + '/reindex',
            method: 'delete'
        })
    }
}