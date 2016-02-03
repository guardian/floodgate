import React from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp from '../components/reactApp.react';
import Reindex from '../components/reindex.react.js';
import Register from '../components/register.react.js';
import Home from '../components/home.react';

export default [
    <Route path="/" component={ReactApp}>
        <Route name="reindex" path="/reindex/:id" component={Reindex} />
        <Route name="register" path="/register" component={Register} />
        <IndexRoute component={Home}/>
    </Route>
];