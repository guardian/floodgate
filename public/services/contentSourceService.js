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
    }


}