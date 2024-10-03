// eslint-disable-next-line no-use-before-define
import React, { useContext, useEffect, useState } from 'react';
import { Chart as ChartJs } from 'chart.js/auto';
import { useParams } from 'react-router-dom';
import { DownOutlined } from '@ant-design/icons';
import { Dropdown, MenuProps, Space } from 'antd/lib';
import { CategoryScale } from 'chart.js';
import './chart.css';
import { ReactChart } from 'chartjs-react';
import 'chartjs-adapter-moment';
import moment from 'moment/moment';
import { Option } from './common';
import { ConfigurationContext } from './main-frame';

type PredefinedPeriod = 'LAST_10_MIN' | 'LAST_HOUR' | 'LAST_MONTH' | 'LAST_YEAR'

ChartJs.register(CategoryScale);

type ChartData = {
  plots: {itemId: string, name: string} []
  name: string,
}

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
  const [data, setData] = useState({
    datasets: [],
  });
  const updateData = async () => {
    let startDate = moment().add(-10, 'minutes');
    switch (predefinedPeriod) {
      case 'LAST_HOUR': {
        startDate = moment().add(-1, 'hours');
        break;
      }
      case 'LAST_MONTH': {
        startDate = moment().add(-1, 'months');
        break;
      }
      case 'LAST_YEAR': {
        startDate = moment().add(-1, 'years');
        break;
      }
      default:
        break;
    }
    const chartParams = params as ChartData;
    const datasets = [];
    // eslint-disable-next-line no-restricted-syntax
    for (const plot of chartParams.plots) {
      // eslint-disable-next-line no-await-in-loop
      const plotData = await ((await fetch('/ui/itemData', {
        method: 'POST',
        body: JSON.stringify({
          itemId: plot.itemId,
          startDate: startDate.toISOString(),
        }),
      })).json()) as {date:string, value:number}[];
      datasets.push({
        label: plot.name,
        data: plotData.map((it) => ({
          x: it.date,
          y: it.value,
        })),
      });
    }
    setData({
      datasets: datasets as any,
    });
  };
  useEffect(() => {
    updateData();
    const timer = window.setInterval(() => updateData(), 5000);
    return () => window.clearInterval(timer);
  }, [predefinedPeriod]);
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
        <ReactChart
          id="unique-chart-id"
          type="line"
          data={data}
          options={
              {
                animation: false,
                scales: {
                  x: {
                    type: 'time',
                    time: {
                      displayFormats: {
                        minute: 'DD HH:mm:ss',
                      },
                    },
                    ticks: { source: 'auto' },
                  },
                  y: {
                    type: 'linear',
                  },
                },
              }
            }
        />
      </div>
    </div>
  );
}
