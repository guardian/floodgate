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
        var id = this.props.contentSource.id;
        this.props.onInitiateReindex(id);
    }

    render () {
        return (
            <div id="content-source">
                <p>Application name: {this.props.contentSource.appName}</p>
                <p>Description: {this.props.contentSource.description}</p>
                <p>Endpoint: {this.props.contentSource.reindexEndpoint}</p>
                <ButtonToolbar>
                    <Button bsStyle="primary" className="pull-right" onClick={this.enterEditMode}>Edit Details</Button>
                    <Button bsStyle="primary" className="pull-right" onClick={this.initiateReindex.bind(this)}>Reindex</Button>
                </ButtonToolbar>
            </div>
        );
    }
}
