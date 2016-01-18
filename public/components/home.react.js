import React from 'react';

export default class ReactApp extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div id="page-wrapper">

                <div className="container-fluid">
                    <h2>Home page.</h2>
                    {this.props.children}
                </div>

            </div>
        );
    }
}