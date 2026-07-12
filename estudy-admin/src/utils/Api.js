import Request from './Request'

const Api = {
  checkCode: '/checkCode',
  login: '/login',
  logout: '/logout',
  getOverviewData: '/getOverviewData',
  getWeekExamData: '/getWeekExamData',
  getStatsData: '/getStatsData',
  getCategoryList: '/getCategoryList',
  editCategory: '/editCategory',
  deleteCategory: '/deleteCategory',
  getPaperList: '/getPaperList',
  editPaper: '/editPaper',
  deletePaper: '/deletePaper',
  getQuestionList: '/getQuestionList',
  editQuestion: '/editQuestion',
  deleteQuestion: '/deleteQuestion',
  getNoComposeQuestions: '/getNoComposeQuestions',
  getQuestionsByPaper: '/getQuestionsByPaper',
  composePaper: '/composePaper',
  getUserList: '/getUserList',
  editUser: '/editUser',
  deleteUser: '/deleteUser',
  getExamRecords: '/getExamRecords',
  getRecordDetail: '/getRecordDetail',
  getSetting: '/getSetting',
  saveSetting: '/saveSetting'
}

export { Api }
