For now, `MGateway` needs know the user requirements in advance. Thus,

* Before sending requests, user requirements should be sent to `/register`. The data should be formatted in json. Please refer to `MUser` class.
* Requests should be sent to `/request` in POST with `MUserRequestBean` json body