import { defineStore } from 'pinia'

export const useQuestionStore = defineStore('questionStore', {
	state: () => {
		return {
			categoryList: [],
			wrongQuestions: [],
			reloadWrongQuestions: false
		}
	},
	actions: {
		setCategoryList(categoryList) {
			this.categoryList = categoryList
		},
		setWrongQuestions(wrongQuestions) {
			this.wrongQuestions = wrongQuestions
		},
		getCategoryList() {
			return this.categoryList
		},
		getWrongQuestions() {
			return this.wrongQuestions
		},
		removeWrongQuestionById(wrongId) {
			this.wrongQuestions = this.wrongQuestions.filter(q => q.wrongId !== wrongId)
		},
		setReloadWrongQuestions(reload) {
			this.reloadWrongQuestions = reload
		},
		getReloadWrongQuestions() {
			return this.reloadWrongQuestions
		}
	}
})