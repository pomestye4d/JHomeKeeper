// eslint-disable-next-line no-use-before-define
import React, { useContext, useState } from 'react';
import { Chart as ChartJs } from 'chart.js/auto';
import { useParams } from 'react-router-dom';
import { DownOutlined } from '@ant-design/icons';
import { Dropdown, MenuProps, Space } from 'antd/lib';
import { Line } from 'react-chartjs-2';
import { CategoryScale } from 'chart.js';
import { ConfigurationContext } from './main-frame';
import './chart.css';
import { Option } from './common';

type PredefinedPeriod = 'LAST_10_MIN' | 'LAST_HOUR' | 'LAST_MONTH' | 'LAST_YEAR'

ChartJs.register(CategoryScale);

const options = [
  {
    id: 'LAST_10_MIN' as PredefinedPeriod,
    displayName: 'Last 10 minutes',
  },
  {
    id: 'LAST_HOUR' as PredefinedPeriod,
    displayName: 'Last hour',
  },
  {
    id: 'LAST_MONTH' as PredefinedPeriod,
    displayName: 'Last month',
  },
  {
    id: 'LAST_YEAR' as PredefinedPeriod,
    displayName: 'Last year',
  },
] as Option[];

export default function Chart() {
  const { id } = useParams();
  const params = useContext(ConfigurationContext).get(id);
  const items: MenuProps['items'] = options.map((opt) => ({
    key: opt.id,
    label: opt.displayName,
  }));
  const [predefinedPeriod, setPredefinedPeriod] = useState('LAST_10_MIN' as PredefinedPeriod);
  const [chartData] = useState({
    labels: [2010, 2011, 2012],
    datasets: [
      {
        borderColor: 'rgb(75, 192, 192)',
        label: 'Plot 1',
        data: [1, 2, 3],
      },
    ],
  });
  return (
    <div className="chart-container">
      <div className="chart-header">
        <div className="chart-title">{params.name}</div>
        <div className="chart-predefined-period">
          <Dropdown menu={{
            items,
            onClick: (item) => {
              setPredefinedPeriod(item.key as PredefinedPeriod);
            },
          }}
          >
            {/* eslint-disable-next-line max-len */}
            {/* eslint-disable-next-line jsx-a11y/no-static-element-interactions,jsx-a11y/anchor-is-valid,jsx-a11y/click-events-have-key-events */}
            <a onClick={(e) => e.preventDefault()}>
              <Space>
                {options.find((it) => it.id === predefinedPeriod)!!.displayName}
                <DownOutlined />
              </Space>
            </a>
          </Dropdown>
        </div>
      </div>
      <div className="chart-content">
        <Line data={chartData} />
      </div>
    </div>
  );
}
