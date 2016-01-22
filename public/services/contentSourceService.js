import Reqwest from 'reqwest';

export default {

    getContentSources:() => {
        return Reqwest({
            url: '/content-source',
            contentType: 'application/json',
            method: 'get'
        })
    },

    getContentSource:(id) => {
        return Reqwest({
            url: '/content-source/' + id,
            contentType: 'application/json',
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
    }
}