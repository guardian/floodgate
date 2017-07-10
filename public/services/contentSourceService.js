import Reqwest from 'reqwest';
import R from 'ramda';

function reindexParams(startDate, endDate) {
  if (!R.isEmpty(startDate) && !R.isEmpty(endDate))
    return `?from=${startDate}&to=${endDate}`
  else if (!R.isEmpty(startDate))
    return `?from=${startDate}`;
  else if (!R.isEmpty(endDate))
    return `?to=${endDate}`;
  else
    return '';
}

export default {

    getContentSources:() => Reqwest({
        url: '/content-source',
        contentType: 'text/json',
        method: 'get'
    }),

    getContentSourcesWithId:(id) => Reqwest({
        url: `/content-source/${id}`,
        contentType: 'text/json',
        method: 'get'
    }),

    createContentSource:(form) => Reqwest({
        url: '/content-source',
        contentType: 'text/json',
        method: 'post',
        data: JSON.stringify(form)
    }),

    updateContentSource:(id, environment, form) => Reqwest({
        url: `/content-source/${id}/${environment}`,
        contentType: 'text/json',
        method: 'put',
        data: JSON.stringify(form)
    }),

    getReindexHistory:(id, environment) => Reqwest({
        url: `/content-source/${id}/${environment}/reindex/history`,
        contentType: 'text/json',
        method: 'get'
    }),

    getRunningReindex:(id, environment) => Reqwest({
        url: `/content-source/${id}/${environment}/reindex/running`,
        contentType: 'text/json',
        method: 'get'
    }),

    initiateReindex:(id, environment, startDate, endDate) => Reqwest({
        url: `/content-source/${id}/${environment}/reindex${reindexParams(startDate, endDate)}`,
        method: 'post'
    }),

    cancelReindex:(id, environment) => Reqwest({
        url: `/content-source/${id}/${environment}/reindex`,
        method: 'delete'
    })
}
