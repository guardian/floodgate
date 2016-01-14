import React from 'react';
import ReactDOM from 'react-dom';
import Router from 'react-router';
import ReactApp from './components/reactApp.react';

import routes from './routes/routes';

ReactDOM.render(<Router routes={routes} />, document.getElementById('react-mount'));
