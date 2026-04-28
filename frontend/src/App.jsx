import React, { useState, useEffect } from 'react';
import { getCourses, getCourseSnapshots, getCourseEvasionPoints, getSignals } from './services/dataService';
import { PageLayout } from './components/layout/PageLayout';
import { OverviewPage } from './pages/OverviewPage';
import { CourseDetailPage } from './pages/CourseDetailPage';
import { LoadingSkeleton } from './components/shared/LoadingSkeleton';
import { LoginPage } from './pages/LoginPage';

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('jwt_token'));
  const [role, setRole] = useState(() => localStorage.getItem('jwt_role'));

  const handleLoginSuccess = (role) => {
    setToken(localStorage.getItem('jwt_token'));
    setRole(role);
  }

  const handleLogout = () => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('jwt_role');
    setToken(null);
    setRole(null);
  }

  const [courses, setCourses] = useState([]);
  const [signals, setSignals] = useState([]);
  
  const [selectedCourseId, setSelectedCourseId] = useState(null);
  
  const [snapshots, setSnapshots] = useState([]);
  const [evasionPoints, setEvasionPoints] = useState([]);
  
  const [loadingCourses, setLoadingCourses] = useState(true);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [error, setError] = useState(null);

  // Carrega visão global (cursos e sinais globais)
  useEffect(() => {
    if (!token) {
      return;
    }

    async function init() {
      try {
        setLoadingCourses(true);
        const [cData, sData] = await Promise.all([
          getCourses(),
          getSignals()
        ]);
        setCourses(cData || []);
        setSignals(sData || []);
      } catch (err) {
        console.error(err);
        setError("Erro ao carregar dados da visão global.");
      } finally {
        setLoadingCourses(false);
      }
    }
    init();
  }, [token]);

  // Carrega detalhes quando um curso é selecionado
  useEffect(() => {
    if (!selectedCourseId) {
      setSnapshots([]);
      setEvasionPoints([]);
      return;
    }

    async function loadDetails() {
      try {
        setLoadingDetails(true);
        const [snapData, evaData] = await Promise.all([
          getCourseSnapshots(selectedCourseId),
          getCourseEvasionPoints(selectedCourseId)
        ]);
        setSnapshots(snapData || []);
        setEvasionPoints(evaData || []);
      } catch (err) {
        if (err?.status === 401) {
          handleLogout();
          return;
        }
        console.error(err);
        // Fallback passivo na UI, limpa os dados
        setSnapshots([]);
        setEvasionPoints([]);
      } finally {
        setLoadingDetails(false);
      }
    }
    loadDetails();
  }, [selectedCourseId]);

  const handleClearFilters = () => {
    setSelectedCourseId(null);
  };

  if (!token) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <PageLayout onClearFilters={handleClearFilters} onLogout={handleLogout} role={role}>
      {loadingCourses ? (
        <LoadingSkeleton rows={4} />
      ) : error ? (
        <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--risk-high-text)' }}>
          {error}
        </div>
      ) : !selectedCourseId ? (
        <OverviewPage
          courses={courses}
          signals={signals}
          selectedCourseId={selectedCourseId}
          onSelectCourse={setSelectedCourseId}
        />
      ) : (
        <CourseDetailPage
          courses={courses}
          snapshots={snapshots}
          evasionPoints={evasionPoints}
          signals={signals}
          selectedCourseId={selectedCourseId}
          onSelectCourse={setSelectedCourseId}
        />
      )}
    </PageLayout>
  );
}

export default App;
