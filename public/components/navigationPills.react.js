import React from 'react';
import { Nav, NavItem } from 'react-bootstrap';

export default class NavigationPills extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {

        var environmentNodes = this.props.contentSources.map( contentSource => {
            const itemEnvironment = contentSource.environment;
            const itemKey = contentSource.id;
            const route = '#/reindex/' + itemKey + '/environment/' + itemEnvironment;
            return(
                <NavItem key={itemKey + '-' + itemEnvironment} eventKey={itemEnvironment} href={route}>{itemEnvironment}</NavItem>
            )
        });

        return (
            <Nav bsStyle="pills" activeKey={this.props.environment}>
                {environmentNodes}
            </Nav>
        );
    }
}
