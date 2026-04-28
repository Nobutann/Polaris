import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

export function RiskDistribution({ courses }) {
  if (!courses || courses.length === 0) return null;

  const data = [
    { name: 'Crítico/Alto', value: 0, color: 'var(--risk-high-text)' },
    { name: 'Médio/Atenção', value: 0, color: 'var(--risk-med-text)' },
    { name: 'Baixo/Engajado', value: 0, color: 'var(--risk-low-text)' }
  ];

  let totalValue = 0;
  courses.forEach(c => {
    data[0].value += (c.highRiskCount || 0);
    data[1].value += (c.medRiskCount || 0);
    data[2].value += (c.lowRiskCount || 0);
    totalValue += (c.highRiskCount || 0) + (c.medRiskCount || 0) + (c.lowRiskCount || 0);
  });

  // Filtra apenas o que tem valor para não quebrar o círculo com padding de itens vazios
  const activeData = data.filter(d => d.value > 0);

  return (
    <div style={{ width: '100%', minHeight: 300, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem', color: 'var(--sebrae-blue)', fontWeight: 700 }}>Distribuição Global de Risco</h3>
      <ResponsiveContainer width="100%" height={240}>
        <PieChart>
          {/* Círculo de fundo para "fechar" o espaço se houver gaps ou poucos dados */}
          <Pie
            data={[{ value: 1 }]}
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={80}
            fill="#f1f5f9"
            stroke="none"
            dataKey="value"
            isAnimationActive={false}
          />
          <Pie
            data={activeData}
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={80}
            paddingAngle={activeData.length > 1 ? 4 : 0}
            dataKey="value"
            stroke="none"
            cornerRadius={4}
            startAngle={90}
            endAngle={-270}
          >
            {activeData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Pie>
          <Tooltip 
            contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
            formatter={(val) => [`${val} alunos`, 'Volume']} 
          />
          <Legend 
            verticalAlign="bottom" 
            height={36} 
            iconType="circle"
            formatter={(value) => <span style={{ color: 'var(--text-main)', fontWeight: 500, fontSize: '0.85rem' }}>{value}</span>}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
