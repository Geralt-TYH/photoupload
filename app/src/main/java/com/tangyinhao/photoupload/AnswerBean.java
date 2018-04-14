package com.tangyinhao.photoupload;

import java.util.List;

public class AnswerBean {
    private int log_id;
    private List<resultBean> result;
    class resultBean{
        private String name;
        private double score;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    public int getLog_id() {
        return log_id;
    }

    public void setLog_id(int log_id) {
        this.log_id = log_id;
    }

    public List<resultBean> getResult() {
        return result;
    }

    public void setResult(List<resultBean> result) {
        this.result = result;
    }
}
