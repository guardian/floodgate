import React from 'react';
import { Link } from 'react-router';

export default class NavigationComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {

        var contentSourceNodes = this.props.data.map(function(contentSource) {
            return (
                <li key={contentSource.id}><Link to={"reindex/" + contentSource.id}>{contentSource.appName}</Link></li>
            );
        });

        return (

            <nav className="navbar navbar-inverse" role="navigation">

                <div className="navbar-header">
                    <button type="button" className="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
                        <span className="sr-only">Toggle navigation</span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                    </button>
                    <Link className="navbar-brand" to="/">Floodgate</Link>
                </div>

                <div className="collapse navbar-collapse navbar-ex1-collapse">
                    <ul className="nav navbar-nav">
                        {contentSourceNodes}
                    </ul>
                </div>
            </nav>
        );
    }
}