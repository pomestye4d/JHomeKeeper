// eslint-disable-next-line no-use-before-define
import React, { useContext } from 'react';
import { useParams } from 'react-router-dom';
import { Col, Row } from 'antd';
import { ConfigurationContext } from './main-frame';
import { views } from './registry';

type ScreenSize = 'SMALL' | 'LARGE'

type ColumnSize = {
    first: ScreenSize,
    second: number,
}

type ColumnData = {
  sizes: ColumnSize[],
  element: any
}

type RowData = {
  columns: ColumnData[]
}

type GridData = {
    id: string,
    name: string,
    rows: RowData[]
}

export default function Grid() {
  const { id } = useParams();
  const params = useContext(ConfigurationContext).get(id) as GridData;

  return (
    <>
      {params.rows.map((row, rowIdx) => (
        <Row key={`row-${rowIdx}`} gutter={16}>
          {row.columns.map((col, idx) => {
            const key = `col-${idx}`;
            return (
              <Col
                key={key}
                xs={{ flex: `${col.sizes.find((it) => it.first === 'SMALL')?.second ?? 100}%` }}
                lg={{ flex: `${col.sizes.find((it) => it.first === 'LARGE')?.second ?? 100}%` }}
              >
                {React.createElement(views.get(col.element.type)!, col.element)}
              </Col>
            );
          })}
        </Row>
      ))}
    </>

  );
}
