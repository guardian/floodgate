import React from 'react';
import ContentSource from './contentSource.react';
import ContentSourceEdit from './contentSourceEdit.react';
import JobHistory from './jobHistory.react';
import ContentSourceService from '../services/contentSourceService';
import { Label, Row, Col, Panel, ProgressBar } from 'react-bootstrap';

export default class ReindexComponent extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            contentSource: {},
            editModeOn: false
        };

        this.updateEditModeState = this.updateEditModeState.bind(this);
        this.loadContentSource = this.loadContentSource.bind(this);
    }

    componentDidMount() {
        var contentSourceId = this.props.params.id;
        this.loadContentSource(contentSourceId);
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.params.id !== nextProps.params.id) {
            this.loadContentSource(nextProps.routeParams.id);
            this.setState({editModeOn: false});
        }
    }

    loadContentSource(id) {
        ContentSourceService.getContentSource(id).then(response => {
            this.setState({
                contentSource: response.contentSource
            });
        });
    }

    updateEditModeState(newState) {
        this.setState({ editModeOn: newState });
        if(newState == false) this.loadContentSource(this.props.params.id);
    }

    render () {

        var jobHistoryStub = [
            {id: 1, status: "Completed", startTime: "2016-01-17T15:17:45.152Z", finishTime: "2016-01-17T15:45:45.152Z"},
            {id: 2, status: "Failed", startTime: "2016-01-10T15:17:45.152Z", finishTime: "2016-01-10T15:45:45.152Z"},
            {id: 3, status: "Completed", startTime: "2016-01-27T15:14:45.152Z", finishTime: "2016-01-27T15:45:45.152Z"},
            {id: 4, status: "Completed", startTime: "2016-01-07T15:17:45.152Z", finishTime: "2016-01-07T15:45:45.152Z"}
        ];

        return (
            <div id="page-wrapper">
                <div className="container-fluid">
                    <Row>
                        <Col xs={12} md={12}>
                            <h3><Label>{this.state.contentSource.appName} Reindexer</Label></h3>
                        </Col>
                        <Col xs={6} md={6}>
                            <Panel header="Details">
                                {this.state.editModeOn ?
                                    <ContentSourceEdit key={this.state.contentSource.id}
                                        contentSource={this.state.contentSource}
                                        callbackParent={this.updateEditModeState} />
                                    :
                                    <ContentSource key={this.state.contentSource.id}
                                        contentSource={this.state.contentSource}
                                        callbackParent={this.updateEditModeState} />
                                }
                            </Panel>
                        </Col>

                        <Col xs={6} md={6}>
                            <Panel header="Running Jobs">
                                <ProgressBar now={60} label="%(percent)s%" />
                            </Panel>
                        </Col>

                        <Col xs={12} md={12}>
                            <Panel header="Reindex History">
                                <JobHistory data={jobHistoryStub}/>
                            </Panel>
                        </Col>
                    </Row>
                </div>
            </div>
        );
    }
}