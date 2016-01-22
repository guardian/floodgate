import React from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp from '../components/reactApp.react';
import Reindex from '../components/reindex.react.js';
import ContentSourceForm from '../components/contentSourceCreate.react.js';
import Home from '../components/home.react';

export default [
    <Route path="/" component={ReactApp}>
        <Route name="reindex" path="/reindex/:id" component={Reindex} />
        <IndexRoute component={Home}/>
    </Route>
];