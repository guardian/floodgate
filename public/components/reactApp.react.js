import React from 'react';
import Reqwest from 'reqwest';
import ContentSourceService from '../services/contentSourceService';
import Navigation from './navigation.react';

export default class ReactApp extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            contentSources: []
        }
    }

    loadContentSources() {
        ContentSourceService.getContentSources().then(response => {
            this.setState({
                contentSources: response.contentSources
            });
        });
    }

    componentDidMount () {
        this.loadContentSources();
    }


    render () {

        return (
            <div id="wrapper">
                <Navigation data={this.state.contentSources}/>
                {this.props.children}
            </div>
        );
    }
}