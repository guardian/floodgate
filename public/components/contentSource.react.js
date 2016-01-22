import React from 'react';
import { Button, ButtonToolbar } from 'react-bootstrap';

export default class ContentSource extends React.Component {

    constructor(props) {
        super(props);
        this.state = {editModeOn: false};
        this.editContentSource = this.editContentSource.bind(this);
    }

    editContentSource() {
        this.setState({editModeOn: true});
        this.props.callbackParent(true);
    }

    render () {
        return (
            <div id="content-source">
                <p>ID: {this.props.id}</p>
                <p>Application name: {this.props.appName}</p>
                <p>Description: {this.props.description}</p>
                <p>Endpoint: {this.props.reindexEndpoint}</p>
                <ButtonToolbar>
                    <Button bsStyle="primary" className="pull-right" onClick={this.editContentSource}>Edit Details</Button>
                    <Button bsStyle="primary" className="pull-right">Reindex</Button>
                </ButtonToolbar>
            </div>
        );
    }
}
