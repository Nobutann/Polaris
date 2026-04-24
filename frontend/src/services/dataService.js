import * as realApi from '../api.js';
import { MOCK_COURSES } from '../mocks/courses.mock.js';
import { MOCK_SNAPSHOTS } from '../mocks/snapshots.mock.js';
import { MOCK_SIGNALS } from '../mocks/signals.mock.js';
import { MOCK_EVASION_POINTS } from '../mocks/evasionPoints.mock.js';

const isDemo = import.meta.env.VITE_DEMO_MODE === 'true';

// Helper to simulate network delay for more realistic demo
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

/**
 * Retorna os cursos e seus dados globais.
 * @returns {Promise<import('../types/course.types').CourseAggregate[]>}
 */
export async function getCourses() {
  if (isDemo) {
    await delay(300);
    return MOCK_COURSES;
  }
  return realApi.fetchCoursesAggregated();
}

/**
 * @param {number} courseId 
 * @returns {Promise<import('../types/course.types').CourseMetricSnapshot[]>}
 */
export async function getCourseSnapshots(courseId) {
  if (isDemo) {
    await delay(300);
    return MOCK_SNAPSHOTS[courseId] || [];
  }
  return realApi.fetchCourseSnapshots(courseId);
}

/**
 * @param {number} courseId 
 * @returns {Promise<import('../types/evasion.types').EvasionPoint[]>}
 */
export async function getCourseEvasionPoints(courseId) {
  if (isDemo) {
    await delay(200);
    return MOCK_EVASION_POINTS[courseId] || [];
  }
  return realApi.fetchEvasionPoints(courseId);
}

/**
 * @param {number} [courseId] 
 * @returns {Promise<import('../types/signal.types').SignalSummary[]>}
 */
export async function getSignals(courseId) {
  if (isDemo) {
    await delay(200);
    let signals = MOCK_SIGNALS;
    if (courseId) {
      signals = signals.filter(s => s.courseId === courseId);
    }
    return signals;
  }
  return realApi.fetchSignals(courseId);
}
