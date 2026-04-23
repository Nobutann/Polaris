import React from 'react';
import { CourseCard } from './CourseCard';

export function CourseList({ courses, selectedCourseId, onSelectCourse }) {
  return (
    <div className="panel" style={{ padding: 0 }}>
      <div style={{ padding: '1.5rem 1.5rem 0.5rem 1.5rem', background: 'var(--sebrae-blue)', color: 'white', borderTopLeftRadius: 8, borderTopRightRadius: 8 }}>
        <h2 style={{ fontSize: '1.1rem', marginBottom: '0.2rem' }}>Cursos (Visão Operacional)</h2>
        <p style={{ fontSize: '0.8rem', color: '#a5c0f3', marginBottom: '1rem' }}>Ordenados por prioridade/risco</p>
      </div>
      <div style={{ padding: '0 1.5rem 1.5rem 1.5rem', maxHeight: '70vh', overflowY: 'auto' }}>
        {courses.map(course => (
          <CourseCard
            key={course.courseId}
            course={course}
            isActive={course.courseId === selectedCourseId}
            onClick={() => onSelectCourse(course.courseId)}
          />
        ))}
      </div>
    </div>
  );
}
