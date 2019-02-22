package anti_captcha;

import org.json.JSONObject;

import anti_captcha.ApiResponse.TaskResultResponse;

public interface IAnticaptchaTaskProtocol {
    JSONObject getPostData();

    TaskResultResponse.SolutionData getTaskSolution();
}
