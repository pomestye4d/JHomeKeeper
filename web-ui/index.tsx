// eslint-disable-next-line no-use-before-define
import React, { FunctionComponent } from 'react';
import { createRoot } from 'react-dom/client';
import { useLocation, useParams } from 'react-router-dom';
import { HomeKeeper, MenuItem } from './src/main-frame';

const root = createRoot(document.getElementById('root') as Element);
const menu = [
  {
    name: 'Graphs',
    children: [
      {
        name: 'Graph 1',
        type: 'graph',
        id: '1',
      },
      {
        name: 'Graph 2',
        type: 'graph',
        id: '2',
      },
    ],
  },
] as MenuItem[];

function BaseComponent() {
  const { id } = useParams();
  return (
    <div>
      params
      {id}
    </div>
  );
}
const views = new Map<string, FunctionComponent>();
views.set('graph', BaseComponent);

root.render(<HomeKeeper menu={menu} views={views} />);
