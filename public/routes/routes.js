import React from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp from '../components/reactApp.react';
import ReindexController from '../components/reindexController.react.js';
import Register from '../components/register.react.js';
import Home from '../components/home.react';

export default [
    <Route path="/" component={ReactApp}>
        <Route name="reindex" path="/reindex/:id" component={ReindexController} />
        <Route name="register" path="/register" component={Register} />
        <IndexRoute component={Home}/>
    </Route>
];