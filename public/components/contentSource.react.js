import React from 'react';
import { Button, ButtonToolbar } from 'react-bootstrap';

export default class ContentSource extends React.Component {

    constructor(props) {
        super(props);
        this.enterEditMode = this.enterEditMode.bind(this);
    }

    enterEditMode() {
        this.props.callbackParent(true);
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
                    <Button bsStyle="primary" className="pull-right">Reindex</Button>
                </ButtonToolbar>
            </div>
        );
    }
}
