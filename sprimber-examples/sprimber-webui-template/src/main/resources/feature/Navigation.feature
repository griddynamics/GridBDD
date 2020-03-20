Feature: WebUI Template suite - Navigation

  @smoke @navigation
  Scenario Outline: Open '<sub-page-name>' page and wait for it to load
    Given open main GridDynamics page
    When navigate to '<sub-page-name>'
    Then '<sub-page-name>' page is opened
    Examples:
      | sub-page-name |
      | Get in Touch  |
      | Careers       |
