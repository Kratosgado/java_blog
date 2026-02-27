package com.kratosgado.blog.backend.controllers.v1;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
  //
  // @Mock
  // private DashboardService dashboardService;
  //
  // @InjectMocks
  // private DashboardController dashboardController;
  //
  // @Test
  // @DisplayName("getStats should delegate to service")
  // void getStats_shouldReturnServiceResult() {
  //   StatCountResponse stats = new StatCountResponse(1, 2, 3, 4, 5);
  //   when(dashboardService.getDashboardStats()).thenReturn(stats);
  //
  //   StatCountResponse result = dashboardController.getStats();
  //
  //   assertThat(result).isSameAs(stats);
  // }
  //
  // @Test
  // @DisplayName("getUserStats should use SecurityUtils current user id")
  // void getUserStats_shouldUseCurrentUserId() {
  //   long userId = 99L;
  //   UserDashboardStatsResponse stats = new UserDashboardStatsResponse(1, 2, 3, 4);
  //   when(dashboardService.getUserDashboardStats(userId)).thenReturn(stats);
  //
  //   try (MockedStatic<SecurityUtils> mocked =
  // org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
  //     mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
  //
  //     UserDashboardStatsResponse result = dashboardController.getUserStats();
  //
  //     assertThat(result).isSameAs(stats);
  //     mocked.verify(SecurityUtils::getCurrentUserId);
  //   }
  // }
  //
  // @Test
  // @DisplayName("getAnalytics should default dates when null")
  // void getAnalytics_shouldDefaultDatesWhenNull() {
  //   AnalyticsResponse analytics = new AnalyticsResponse(0, 0L, 0.0, List.of());
  //   when(dashboardService.getAnalytics(any(LocalDateTime.class), any(LocalDateTime.class)))
  //       .thenReturn(analytics);
  //
  //   AnalyticsResponse result = dashboardController.getAnalytics(null, null);
  //
  //   assertThat(result).isSameAs(analytics);
  // }
  //
  // @Test
  // @DisplayName("getPostDistribution should delegate to service")
  // void getPostDistribution_shouldReturnServiceResult() {
  //   PostDistributionResponse distribution = new PostDistributionResponse(1, 2, 3);
  //   when(dashboardService.getPostStatusDistribution()).thenReturn(distribution);
  //
  //   PostDistributionResponse result = dashboardController.getPostDistribution();
  //
  //   assertThat(result).isSameAs(distribution);
  // }
  //
  // @Test
  // @DisplayName("getEngagement should delegate to service")
  // void getEngagement_shouldReturnServiceResult() {
  //   EngagementStatsResponse stats = new EngagementStatsResponse(List.of(), List.of(), List.of());
  //   when(dashboardService.getEngagementStats()).thenReturn(stats);
  //
  //   EngagementStatsResponse result = dashboardController.getEngagement();
  //
  //   assertThat(result).isSameAs(stats);
  // }
  //
  // @Test
  // @DisplayName("getRecentActivity should delegate to service")
  // void getRecentActivity_shouldReturnServiceResult() {
  //   RecentActivityResponse recent =
  //       new RecentActivityResponse(List.of(), List.of());
  //   when(dashboardService.getRecentActivity()).thenReturn(recent);
  //
  //   RecentActivityResponse result = dashboardController.getRecentActivity();
  //
  //   assertThat(result).isSameAs(recent);
  // }
}
