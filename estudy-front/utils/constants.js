const DIFFICULTY_TYPE = [{
		label: '全部',
		value: 'all'
	},
	{
		label: '简单',
		value: 'easy'
	},
	{
		label: '中等',
		value: 'medium'
	},
	{
		label: '困难',
		value: 'hard'
	}
]

const QUESTION_TYPE = {
	0: '单选题',
	1: '多选题',
	2: '判断题'
}

const LOCAL_STORAGE_KEY = {
	'token': {
		name: 'token',
		key: 'token'
	},
	'searchHistory': {
		name: '搜索历史',
		key: 'searchHistory'
	}
}

export {
	DIFFICULTY_TYPE,
	QUESTION_TYPE,
	LOCAL_STORAGE_KEY
}