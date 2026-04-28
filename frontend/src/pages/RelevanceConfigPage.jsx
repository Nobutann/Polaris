import React, { useState, useEffect } from 'react';
import { getRelevanceWeights, updateRelevanceWeight, resetRelevanceWeights } from '../services/dataService';

const SIGNAL_META = {
  INACTIVITY: { label: 'Inatividade', desc: 'Tempo sem atividade recente no curso' },
  CONTINUITY: { label: 'Continuidade', desc: 'Regularidade de progresso entre etapas' },
  COMPLETION: { label: 'Conclusão', desc: 'Avanço em direção ao término do curso' },
  ADVANCE_DEPTH: { label: 'Profundidade de avanço', desc: 'Quanto o usuário realmente avançou no conteúdo' }
};

export function RelevanceConfigPage({ onBack }) {
  const [weights, setWeights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  const [editingKey, setEditingKey] = useState(null);
  const [editForm, setEditForm] = useState({ weight: 0, enabled: true });

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getRelevanceWeights();
      setWeights(data);
    } catch (err) {
      setError(err.message || 'Erro ao carregar os pesos');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleEdit = (item) => {
    setEditingKey(item.signalKey);
    setEditForm({ weight: item.weight, enabled: item.enabled });
  };

  const handleCancel = () => {
    setEditingKey(null);
  };

  const handleSave = async (signalKey) => {
    try {
      setError(null);
      setSuccess(null);
      await updateRelevanceWeight(signalKey, { weight: editForm.weight, enabled: editForm.enabled });
      setSuccess(`Sinal ${SIGNAL_META[signalKey]?.label || signalKey} atualizado com sucesso!`);
      setEditingKey(null);
      await loadData();
    } catch (err) {
      setError(err.message || 'Erro ao salvar alterações.');
    }
  };

  const handleReset = async () => {
    if (!window.confirm("Deseja realmente restaurar os pesos para o padrão?")) return;
    try {
      setError(null);
      setSuccess(null);
      setLoading(true);
      await resetRelevanceWeights();
      setSuccess("Pesos restaurados para o padrão.");
      await loadData();
    } catch (err) {
      setError(err.message || 'Erro ao resetar pesos.');
      setLoading(false);
    }
  };

  return (
    <div style={{ background: 'var(--bg-page)', minHeight: '100vh', paddingBottom: '2rem' }}>
      <header className="top-header" style={{ justifyContent: 'flex-start', gap: '2rem' }}>
        <button 
          onClick={onBack}
          style={{ 
            background: 'transparent', border: '1px solid rgba(255,255,255,0.3)', 
            color: 'white', padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer',
            fontWeight: '600'
          }}
        >
          ← Voltar ao Dashboard
        </button>
        <div className="brand">
          <div className="brand-logo">SEBRAE</div>
          <div className="brand-titles">
            <h1>Configuração de Relevância</h1>
            <p>Ajuste os pesos e ativação dos sinais de risco operacional.</p>
          </div>
        </div>
      </header>

      <main style={{ maxWidth: '900px', margin: '2rem auto', padding: '0 1.5rem' }}>
        
        {error && (
          <div style={{ background: 'var(--risk-high-bg)', color: 'var(--risk-high-text)', padding: '1rem', borderRadius: '6px', marginBottom: '1rem', fontWeight: '500' }}>
            {error}
          </div>
        )}
        
        {success && (
          <div style={{ background: 'var(--risk-low-bg)', color: 'var(--risk-low-text)', padding: '1rem', borderRadius: '6px', marginBottom: '1rem', fontWeight: '500' }}>
            {success}
          </div>
        )}

        <div className="panel" style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
            <div>
              <h2 className="panel-header" style={{ marginBottom: '0.25rem' }}>Pesos dos Sinais</h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Defina a importância de cada sinal no cálculo de risco. 
                <br/><strong>Atenção:</strong> As alterações impactam os próximos cálculos de risco da plataforma.
              </p>
            </div>
            <button 
              onClick={handleReset}
              disabled={loading}
              style={{
                background: 'var(--surface)', border: '1px solid var(--border)', padding: '0.5rem 1rem', 
                borderRadius: '6px', cursor: loading ? 'not-allowed' : 'pointer', fontSize: '0.85rem', fontWeight: '600',
                color: 'var(--text-muted)', transition: 'background 0.2s'
              }}
              onMouseOver={e => e.currentTarget.style.background = '#f8fafc'}
              onMouseOut={e => e.currentTarget.style.background = 'var(--surface)'}
            >
              Restaurar Padrão
            </button>
          </div>

          {loading && !weights.length ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>Carregando configurações...</div>
          ) : (
            <div style={{ border: '1px solid var(--border)', borderRadius: '8px', overflow: 'hidden' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
                <thead style={{ background: '#f8fafc' }}>
                  <tr>
                    <th style={{ textAlign: 'left', padding: '1rem', borderBottom: '1px solid var(--border)', fontWeight: '600', color: 'var(--text-muted)' }}>Sinal</th>
                    <th style={{ textAlign: 'center', padding: '1rem', borderBottom: '1px solid var(--border)', fontWeight: '600', color: 'var(--text-muted)' }}>Peso</th>
                    <th style={{ textAlign: 'center', padding: '1rem', borderBottom: '1px solid var(--border)', fontWeight: '600', color: 'var(--text-muted)' }}>Status</th>
                    <th style={{ textAlign: 'right', padding: '1rem', borderBottom: '1px solid var(--border)', fontWeight: '600', color: 'var(--text-muted)' }}>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {weights.map((item) => {
                    const isEditing = editingKey === item.signalKey;
                    const meta = SIGNAL_META[item.signalKey] || { label: item.signalKey, desc: '' };

                    return (
                      <tr key={item.signalKey} style={{ borderBottom: '1px solid var(--border)', background: isEditing ? '#f0f9ff' : 'transparent' }}>
                        <td style={{ padding: '1rem' }}>
                          <div style={{ fontWeight: '600', color: 'var(--text-main)', marginBottom: '0.2rem' }}>{meta.label}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{meta.desc}</div>
                        </td>
                        
                        <td style={{ padding: '1rem', textAlign: 'center' }}>
                          {isEditing ? (
                            <input 
                              type="number" 
                              value={editForm.weight}
                              onChange={(e) => setEditForm({...editForm, weight: Number(e.target.value)})}
                              style={{ width: '80px', padding: '0.4rem', border: '1px solid var(--sebrae-light-blue)', borderRadius: '4px', textAlign: 'center' }}
                            />
                          ) : (
                            <span style={{ fontWeight: '700', fontSize: '1rem' }}>{item.weight}</span>
                          )}
                        </td>

                        <td style={{ padding: '1rem', textAlign: 'center' }}>
                          {isEditing ? (
                            <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                              <input 
                                type="checkbox" 
                                checked={editForm.enabled}
                                onChange={(e) => setEditForm({...editForm, enabled: e.target.checked})}
                                style={{ width: '16px', height: '16px' }}
                              />
                              <span style={{ fontSize: '0.85rem' }}>{editForm.enabled ? 'Ativo' : 'Inativo'}</span>
                            </label>
                          ) : (
                            <span className={`badge ${item.enabled ? 'green' : 'red'}`} style={{ display: 'inline-block' }}>
                              {item.enabled ? 'ATIVO' : 'INATIVO'}
                            </span>
                          )}
                        </td>

                        <td style={{ padding: '1rem', textAlign: 'right' }}>
                          {isEditing ? (
                            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                              <button 
                                onClick={() => handleSave(item.signalKey)}
                                style={{ background: 'var(--sebrae-light-blue)', color: 'white', border: 'none', padding: '0.4rem 0.8rem', borderRadius: '4px', cursor: 'pointer', fontWeight: '600', fontSize: '0.8rem' }}
                              >
                                Salvar
                              </button>
                              <button 
                                onClick={handleCancel}
                                style={{ background: 'transparent', color: 'var(--text-muted)', border: '1px solid var(--border)', padding: '0.4rem 0.8rem', borderRadius: '4px', cursor: 'pointer', fontWeight: '600', fontSize: '0.8rem' }}
                              >
                                Cancelar
                              </button>
                            </div>
                          ) : (
                            <button 
                              onClick={() => handleEdit(item)}
                              disabled={editingKey !== null}
                              style={{ background: 'transparent', color: 'var(--sebrae-light-blue)', border: '1px solid var(--sebrae-light-blue)', padding: '0.4rem 1rem', borderRadius: '4px', cursor: editingKey !== null ? 'not-allowed' : 'pointer', fontWeight: '600', fontSize: '0.8rem', opacity: editingKey !== null ? 0.5 : 1 }}
                            >
                              Editar
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
