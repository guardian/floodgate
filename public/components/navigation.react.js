import React from 'react';
import { Link } from 'react-router';
import { Navbar, Nav, NavItem, NavDropdown, MenuItem, Label } from 'react-bootstrap'

export default class NavigationComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        let distinctContentSources = [];
        const allContentSources = this.props.data;

        allContentSources.forEach(contentSource => {
            if (!distinctContentSources[contentSource.id])
                distinctContentSources[contentSource.id] = { appName: contentSource.appName, environment: contentSource.environment };
        });

        const contentSourceNodes = Object.keys(distinctContentSources).map(function (itemKey) {
            const appName = distinctContentSources[itemKey].appName;
            const environment = distinctContentSources[itemKey].environment;
            const route = '#/reindex/' + itemKey + '/environment/' + environment;
            return (
                <MenuItem eventKey={itemKey} key={itemKey} href={route}>{appName}</MenuItem>
            );
        });

        return (
            <Navbar inverse>
                <Navbar.Header>
                    <Navbar.Brand><Link className="navbar-brand" to="/">Floodgate</Link></Navbar.Brand>
                    <Navbar.Toggle />
                </Navbar.Header>
                <Navbar.Collapse>
                    <Nav>
                        <NavItem eventKey={1} href="#/register">Register</NavItem>
                        <NavDropdown eventKey={2} title="Content sources" id="nav-content-source-dropdown">
                            {contentSourceNodes}
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        );
    }
}