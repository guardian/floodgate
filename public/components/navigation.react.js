import React from 'react';
import { Link } from 'react-router';
import { Navbar, Nav, NavItem, NavDropdown, MenuItem, Label } from 'react-bootstrap'

export default class NavigationComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        var distinctContentSources = [];
        var allContentSources = this.props.data;
        for(var i = 0; i < allContentSources.length; i++) {
            if(distinctContentSources[allContentSources[i].id] == undefined)
                distinctContentSources[allContentSources[i].id] = { appName: allContentSources[i].appName, environment: allContentSources[i].environment };
        }

        var contentSourceNodes = Object.keys(distinctContentSources).map(function (itemKey) {
            var appName = distinctContentSources[itemKey].appName;
            var environment = distinctContentSources[itemKey].environment;
            var route = '#/reindex/' + itemKey + '/environment/' + environment;
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