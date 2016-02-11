import React from 'react';
import ContentSourceService from '../services/contentSourceService';
import { Button, ButtonToolbar } from 'react-bootstrap';

export default class ContentSource extends React.Component {

    constructor(props) {
        super(props);
        this.enterEditMode = this.enterEditMode.bind(this);
        this.initiateReindex = this.initiateReindex.bind(this);
    }

    enterEditMode() {
        this.props.callbackParent(true);
    }

    initiateReindex() {
        ContentSourceService.initiateReindex(this.props.contentSource.id).then(response => {
            console.log(response);
        },
        error => {
            console.log(error.response);
        })
    }

    render () {
        return (
            <div id="content-source">
                <p>ID: {this.props.contentSource.id}</p>
                <p>Application name: {this.props.contentSource.appName}</p>
                <p>Description: {this.props.contentSource.description}</p>
                <p>Endpoint: {this.props.contentSource.reindexEndpoint}</p>
                <ButtonToolbar>
                    <Button bsStyle="primary" className="pull-right" onClick={this.enterEditMode}>Edit Details</Button>
                    <Button bsStyle="primary" className="pull-right" onClick={this.initiateReindex}>Reindex</Button>
                </ButtonToolbar>
            </div>
        );
    }
}
