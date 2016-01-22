import React from 'react';
import ContentSourceForm from './contentSourceCreate.react.js';
import { PageHeader } from 'react-bootstrap';

export default class ReactApp extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div id="page-wrapper">

                <div className="container-fluid">
                    <PageHeader>Create content source.</PageHeader>
                    {this.props.children}
                </div>

                <ContentSourceForm />

            </div>
        );
    }
}