import React from 'react';
import ContentSourceForm from './contentSourceCreate.react.js';
import { Panel, Col, Row, Label } from 'react-bootstrap';

export default class ReactApp extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div id="page-wrapper">
                <div className="container-fluid">
                    <Row>
                        <Col xs={12} md={12}>
                            <h3><Label>Register</Label></h3>
                        </Col>
                        <Col xs={12} md={8}>
                            <Panel header="Create Content Source">
                                <div className="container-fluid">
                                    {this.props.children}
                                </div>
                                <ContentSourceForm />
                            </Panel>
                        </Col>
                    </Row>
                </div>
            </div>
        );
    }
}