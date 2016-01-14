import React from 'react';
import { Link } from 'react-router';
import { Navbar } from 'react-bootstrap';

export default class Header extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {

        return (
            <Navbar>
                <Navbar.Header>
                    <Navbar.Brand>
                        <Link to="/">Floodgate</Link>
                    </Navbar.Brand>
                </Navbar.Header>
                <Navbar.Collapse>
                    <Navbar.Text><Link to="/other">Other</Link></Navbar.Text>
                </Navbar.Collapse>
            </Navbar>
        );
    }
}