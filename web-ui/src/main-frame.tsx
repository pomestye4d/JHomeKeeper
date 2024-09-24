/* eslint-disable */
// eslint-disable-next-line no-use-before-define
import React, {FunctionComponent} from 'react';
import {ProLayout} from '@ant-design/pro-layout';
import {BrowserRouter, Link, Outlet, Route, Routes, useNavigate} from "react-router-dom";
import {NavigateFunction} from "react-router/dist/lib/hooks";

export type MenuItem = {
    name: string,
    id?: string,
    type: string,
    children?: MenuItem[]
}

export type MainFrameProps = {
    // eslint-disable-next-line no-unused-vars
    views: Map<string, FunctionComponent>
    menu: MenuItem[]
}

export function HomeKeeper(props: MainFrameProps) {
    return (<BrowserRouter>
        <App views={props.views} menu={props.menu}/>
    </BrowserRouter>)
}


function App(props: MainFrameProps) {
    const nav = useNavigate();
    return <Routes>
        <Route path="/" element={MainFrameComponent({...props, navigate: nav})}>
            <Route key="/" path="/" element={<div>Welcome</div>}/>
            {[...props.views.entries()].map(entry => {
                return <Route key={entry[0]} path={entry[0]}>
                    <Route key={entry[0]} path=":id" element={React.createElement(entry[1], {})}/>
                </Route>
            })
            }
        </Route>
    </Routes>
}

export function MainFrameComponent(props: MainFrameProps & { navigate: NavigateFunction }) {
    const createItem = (item: MenuItem) => ({
        name: item.name,
        path: `${item.type}/${item.id}`,
    });
    return (
        <div
            id="test-pro-layout"
            style={{
                height: '100vh',
                overflow: 'auto',
            }}
        >
            <ProLayout
                title="Home Keeper"
                onMenuHeaderClick={() => {
                    props.navigate("/");
                }}
                style={{
                    height: '100vh',
                }}
                menu={{
                    collapsedShowGroupTitle: true,
                }}
                location={{
                    pathname: "welcome",
                }}
                route={
                    {
                        children: props.menu.map(it => {
                            if ((it.children?.length ?? 0) > 0) {
                                return {
                                    name: it.name,
                                    key: it.name,
                                    children: it.children!!.map(ch => createItem(ch))
                                }
                            }
                            return createItem(it)
                        })
                    }
                }
                openKeys={props.menu.filter(it => (it.children?.length ?? 0) > 0).map(it => it.name)}
                menuItemRender={(item, dom) => (
                    <div
                    >
                        <Link to={item.path || '/'}>{item.name}</Link>
                    </div>
                )}
            >
                <Outlet/>
            </ProLayout>
        </div>
    );
}
