import React from 'react';
import { Button, ButtonToolbar } from 'react-bootstrap';
import Moment from 'moment';

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
                <p><strong>Application name:</strong> {this.props.contentSource.appName}</p>
                <p><strong>Description:</strong> {this.props.contentSource.description}</p>
                <p><strong>Environment:</strong> {this.props.contentSource.environment}</p>
                <p><strong>Auth type:</strong> {this.props.contentSource.authType}</p>

                <ButtonToolbar>
                    <Button bsStyle="primary" className="pull-right" onClick={this.enterEditMode}> Edit Details</Button>
                </ButtonToolbar>
            </div>
        );
    }
}
