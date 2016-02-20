import Reqwest from 'reqwest';

export default {

    getContentSources:() => {
        return Reqwest({
            url: '/content-source',
            contentType: 'text/json',
            method: 'get'
        })
    },

    getContentSource:(id) => {
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

    updateContentSource:(id, form) => {
        return Reqwest({
            url: '/content-source/' + id,
            contentType: 'text/json',
            method: 'put',
            data: JSON.stringify(form)
        })
    },

    getReindexHistory:(id) => {
        return Reqwest({
            url: '/content-source/' + id + '/reindex/history',
            contentType: 'text/json',
            method: 'get'
        })
    },

    getRunningReindexes:(id) => {
        return Reqwest({
            url: '/content-source/' + id + '/reindex/running',
            contentType: 'text/json',
            method: 'get'
        })
    },

    initiateReindex:(id, startDate, endDate) => {
        var url = '/content-source/' + id + '/reindex';
        if(startDate != '' && endDate === '') url += '?from=' + startDate;
        else if(startDate === '' && endDate != '') url += '?to=' + endDate;
        else if(startDate != '' && endDate != '') url += '?from=' + startDate + "&to=" + endDate;

        return Reqwest({
            url: url,
            method: 'post'
        })
    },

    cancelReindex:(id) => {
        return Reqwest({
            url: '/content-source/' + id + '/reindex',
            method: 'delete'
        })
    }
}