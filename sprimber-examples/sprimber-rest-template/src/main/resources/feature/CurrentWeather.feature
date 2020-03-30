Feature: Rest Template suite

  @smoke @current-weather
  Scenario Outline: Check current weather for <country> and <city>
    When Weather rest service is called with following values:
      | requestId | country   | city   |
      | 1         | <country> | <city> |
    Then following calls are successful:
      | requestId |
      | 1         |
    And temperature and humidity values are present for calls:
      | requestId |
      | 1         |
    Examples:
      | country | city   |
      | Poland  | Cracow |
      | Poland  | Warsaw |

  @smoke @current-weather
  Scenario: Check current weather for multiple places
    When Weather rest service is called with following values:
      | requestId | country | city   |
      | 1         | Poland  | Cracow |
      | 2         | Poland  | Warsaw |
    Then following calls are successful:
      | requestId |
      | 1         |
      | 2         |
    Then temperature and humidity values are present for calls:
      | requestId |
      | 1         |
      | 2         |

  @smoke @current-weather @failed
  Scenario: Check current weather for unknown place
    When Weather rest service is called with following values:
      | requestId | country | city  |
      | 1         | Poland  | Paris |
    Then following calls are failed with status code:
      | requestId | statusCode |
      | 1         | 404        |
