import React from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp from '../components/reactApp.react';
import Other from '../components/other.react';
import Home from '../components/home.react';

export default [
    <Route path="/" component={ReactApp}>
        <Route name="other" path="/other" component={Other} />
        <IndexRoute component={Home}/>
    </Route>
];