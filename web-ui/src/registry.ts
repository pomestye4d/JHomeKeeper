import { FunctionComponent } from 'react';
import Chart from './chart';
import Button from './button';
import Label from './label';
import Grid from './grid';

// eslint-disable-next-line import/prefer-default-export
export const views = new Map<string, FunctionComponent<any>>();
views.set('CHART', Chart);
views.set('BUTTON', Button);
views.set('LABEL', Label);
views.set('GRID', Grid);
