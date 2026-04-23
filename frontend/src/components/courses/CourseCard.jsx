import React from 'react';
import { RiskBadge } from './RiskBadge';

export function CourseCard({ course, isActive, onClick }) {
  return (
    <div className={`list-item ${isActive ? 'active' : ''}`} onClick={onClick}>
      <div className="item-icon">📚</div>
      <div className="item-info">
        <div className="item-title" style={{ fontWeight: isActive ? 700 : 600 }}>{course.courseName || `Curso ${course.courseId}`}</div>
        <div className="item-sub">{course.totalEnrolled} matriculados • {course.category || 'Geral'}</div>
      </div>
      <div className="item-badge">
        <RiskBadge level={course.riskLevel} />
        {course.riskLevel !== 'BAIXO' && (
          <span style={{ fontSize: '0.8rem', fontWeight: 600 }}>
            {course.highRiskCount} vols
          </span>
        )}
      </div>
    </div>
  );
}
