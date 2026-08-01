locals {
  name_prefix = "${var.project_name}-${var.environment}"

  services = {
    frontend = {
      build_project_name      = "${local.name_prefix}-frontend-build"
      buildspec               = "buildspec-frontend.yml"
      ecr_repository_arn      = var.ecr_repository_arns.frontend
      codedeploy_app_name     = "${local.name_prefix}-frontend-deploy"
      deployment_group_name   = "${local.name_prefix}-frontend-dg"
      ecs_service_name        = var.ecs_service_names.frontend
      blue_target_group_name  = var.target_group_names.frontend_blue
      green_target_group_name = var.target_group_names.frontend_green
      listener_arn            = var.listener_arns.frontend
      container_name          = "frontend"
      taskdef_artifact        = "FrontendBuildOutput"
    }
    backend = {
      build_project_name      = "${local.name_prefix}-backend-build"
      buildspec               = "buildspec-backend.yml"
      ecr_repository_arn      = var.ecr_repository_arns.backend
      codedeploy_app_name     = "${local.name_prefix}-backend-deploy"
      deployment_group_name   = "${local.name_prefix}-backend-dg"
      ecs_service_name        = var.ecs_service_names.backend
      blue_target_group_name  = var.target_group_names.backend_blue
      green_target_group_name = var.target_group_names.backend_green
      listener_arn            = var.listener_arns.backend
      container_name          = "backend"
      taskdef_artifact        = "BackendBuildOutput"
    }
  }
}

data "aws_caller_identity" "current" {}

data "aws_partition" "current" {}

data "aws_iam_policy_document" "codebuild_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["codebuild.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "codedeploy_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["codedeploy.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "codepipeline_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["codepipeline.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "codebuild" {
  name               = "${local.name_prefix}-codebuild-role"
  assume_role_policy = data.aws_iam_policy_document.codebuild_assume_role.json

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-codebuild-role"
  })
}

data "aws_iam_policy_document" "codebuild" {
  statement {
    sid = "WriteBuildLogs"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = ["arn:${data.aws_partition.current.partition}:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/codebuild/${local.name_prefix}-*"]
  }

  statement {
    sid = "UsePipelineArtifacts"
    actions = [
      "s3:GetObject",
      "s3:GetObjectVersion",
      "s3:PutObject",
      "s3:GetBucketVersioning"
    ]
    resources = [
      var.artifact_bucket_arn,
      "${var.artifact_bucket_arn}/*"
    ]
  }

  statement {
    sid       = "GetEcrToken"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid = "PushImages"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:CompleteLayerUpload",
      "ecr:DescribeImages",
      "ecr:DescribeRepositories",
      "ecr:GetDownloadUrlForLayer",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart"
    ]
    resources = [
      var.ecr_repository_arns.frontend,
      var.ecr_repository_arns.backend
    ]
  }

  statement {
    sid       = "IdentifyAccount"
    actions   = ["sts:GetCallerIdentity"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "codebuild" {
  name   = "${local.name_prefix}-codebuild-policy"
  role   = aws_iam_role.codebuild.id
  policy = data.aws_iam_policy_document.codebuild.json
}

resource "aws_codebuild_project" "service" {
  for_each = local.services

  name         = each.value.build_project_name
  description  = "Build and package ${each.key} image and CodeDeploy ECS artifacts."
  service_role = aws_iam_role.codebuild.arn

  artifacts {
    type = "CODEPIPELINE"
  }

  environment {
    compute_type                = "BUILD_GENERAL1_SMALL"
    image                       = "aws/codebuild/standard:7.0"
    type                        = "LINUX_CONTAINER"
    privileged_mode             = true
    image_pull_credentials_type = "CODEBUILD"

    environment_variable {
      name  = "AWS_DEFAULT_REGION"
      value = var.aws_region
    }

    environment_variable {
      name  = "BACKEND_ALB_DNS_NAME"
      value = var.backend_alb_dns_name
    }

    environment_variable {
      name  = "DYNAMODB_TABLE_NAME"
      value = var.dynamodb_table_name
    }

    environment_variable {
      name  = "FRONTEND_EXECUTION_ROLE_ARN"
      value = var.ecs_task_execution_role_arn
    }

    environment_variable {
      name  = "FRONTEND_TASK_ROLE_ARN"
      value = var.frontend_task_role_arn
    }

    environment_variable {
      name  = "BACKEND_EXECUTION_ROLE_ARN"
      value = var.ecs_task_execution_role_arn
    }

    environment_variable {
      name  = "BACKEND_TASK_ROLE_ARN"
      value = var.backend_task_role_arn
    }
  }

  logs_config {
    cloudwatch_logs {
      group_name  = "/aws/codebuild/${each.value.build_project_name}"
      stream_name = "build"
    }
  }

  source {
    type      = "CODEPIPELINE"
    buildspec = each.value.buildspec
  }

  tags = merge(var.common_tags, {
    Name    = each.value.build_project_name
    Service = each.key
  })
}

resource "aws_iam_role" "codedeploy" {
  name               = "${local.name_prefix}-codedeploy-role"
  assume_role_policy = data.aws_iam_policy_document.codedeploy_assume_role.json

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-codedeploy-role"
  })
}

resource "aws_iam_role_policy_attachment" "codedeploy_ecs" {
  role       = aws_iam_role.codedeploy.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AWSCodeDeployRoleForECS"
}

resource "aws_codedeploy_app" "service" {
  for_each = local.services

  name             = each.value.codedeploy_app_name
  compute_platform = "ECS"

  tags = merge(var.common_tags, {
    Name    = each.value.codedeploy_app_name
    Service = each.key
  })
}

resource "aws_codedeploy_deployment_group" "service" {
  for_each = local.services

  app_name               = aws_codedeploy_app.service[each.key].name
  deployment_group_name  = each.value.deployment_group_name
  deployment_config_name = "CodeDeployDefault.ECSAllAtOnce"
  service_role_arn       = aws_iam_role.codedeploy.arn

  deployment_style {
    deployment_option = "WITH_TRAFFIC_CONTROL"
    deployment_type   = "BLUE_GREEN"
  }

  ecs_service {
    cluster_name = var.ecs_cluster_name
    service_name = each.value.ecs_service_name
  }

  load_balancer_info {
    target_group_pair_info {
      prod_traffic_route {
        listener_arns = [each.value.listener_arn]
      }

      target_group {
        name = each.value.blue_target_group_name
      }

      target_group {
        name = each.value.green_target_group_name
      }
    }
  }

  blue_green_deployment_config {
    deployment_ready_option {
      action_on_timeout = "CONTINUE_DEPLOYMENT"
    }

    terminate_blue_instances_on_deployment_success {
      action                           = "TERMINATE"
      termination_wait_time_in_minutes = 5
    }
  }

  auto_rollback_configuration {
    enabled = true
    events = [
      "DEPLOYMENT_FAILURE",
      "DEPLOYMENT_STOP_ON_ALARM",
      "DEPLOYMENT_STOP_ON_REQUEST"
    ]
  }

  tags = merge(var.common_tags, {
    Name    = each.value.deployment_group_name
    Service = each.key
  })

  depends_on = [aws_iam_role_policy_attachment.codedeploy_ecs]
}

resource "aws_iam_role" "codepipeline" {
  name               = "${local.name_prefix}-codepipeline-role"
  assume_role_policy = data.aws_iam_policy_document.codepipeline_assume_role.json

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-codepipeline-role"
  })
}

data "aws_iam_policy_document" "codepipeline" {
  statement {
    sid = "UseCodeConnection"
    actions = [
      "codestar-connections:UseConnection",
      "codeconnections:UseConnection"
    ]
    resources = [var.codeconnection_arn]
  }

  statement {
    sid = "UseArtifactBucket"
    actions = [
      "s3:GetObject",
      "s3:GetObjectVersion",
      "s3:GetBucketVersioning",
      "s3:PutObject",
      "s3:PutObjectAcl"
    ]
    resources = [
      var.artifact_bucket_arn,
      "${var.artifact_bucket_arn}/*"
    ]
  }

  statement {
    sid = "RunCodeBuild"
    actions = [
      "codebuild:BatchGetBuilds",
      "codebuild:StartBuild"
    ]
    resources = [for project in aws_codebuild_project.service : project.arn]
  }

  statement {
    sid = "RunCodeDeploy"
    actions = [
      "codedeploy:CreateDeployment",
      "codedeploy:GetApplication",
      "codedeploy:GetApplicationRevision",
      "codedeploy:GetDeployment",
      "codedeploy:GetDeploymentConfig",
      "codedeploy:GetDeploymentGroup",
      "codedeploy:RegisterApplicationRevision"
    ]
    resources = ["*"]
  }

  statement {
    sid = "RegisterEcsTaskDefinitions"
    actions = [
      "ecs:DescribeTaskDefinition",
      "ecs:RegisterTaskDefinition"
    ]
    resources = ["*"]
  }

  statement {
    sid     = "PassEcsRoles"
    actions = ["iam:PassRole"]
    resources = [
      var.ecs_task_execution_role_arn,
      var.frontend_task_role_arn,
      var.backend_task_role_arn
    ]
  }
}

resource "aws_iam_role_policy" "codepipeline" {
  name   = "${local.name_prefix}-codepipeline-policy"
  role   = aws_iam_role.codepipeline.id
  policy = data.aws_iam_policy_document.codepipeline.json
}

resource "aws_codepipeline" "main" {
  name          = "${local.name_prefix}-pipeline"
  role_arn      = aws_iam_role.codepipeline.arn
  pipeline_type = "V2"

  artifact_store {
    location = var.artifact_bucket_name
    type     = "S3"
  }

  stage {
    name = "Source"

    action {
      name             = "GitHubSource"
      category         = "Source"
      owner            = "AWS"
      provider         = "CodeStarSourceConnection"
      version          = "1"
      output_artifacts = ["SourceOutput"]

      configuration = {
        ConnectionArn        = var.codeconnection_arn
        FullRepositoryId     = var.repository_full_name
        BranchName           = var.repository_branch
        DetectChanges        = "true"
        OutputArtifactFormat = "CODE_ZIP"
      }
    }
  }

  stage {
    name = "ParallelBuild"

    action {
      name             = "BuildFrontend"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      input_artifacts  = ["SourceOutput"]
      output_artifacts = ["FrontendBuildOutput"]
      version          = "1"
      run_order        = 1

      configuration = {
        ProjectName = aws_codebuild_project.service["frontend"].name
      }
    }

    action {
      name             = "BuildBackend"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      input_artifacts  = ["SourceOutput"]
      output_artifacts = ["BackendBuildOutput"]
      version          = "1"
      run_order        = 1

      configuration = {
        ProjectName = aws_codebuild_project.service["backend"].name
      }
    }
  }

  stage {
    name = "ParallelBlueGreenDeploy"

    action {
      name            = "DeployFrontend"
      category        = "Deploy"
      owner           = "AWS"
      provider        = "CodeDeployToECS"
      input_artifacts = ["FrontendBuildOutput"]
      version         = "1"
      run_order       = 1

      configuration = {
        ApplicationName                = aws_codedeploy_app.service["frontend"].name
        DeploymentGroupName            = aws_codedeploy_deployment_group.service["frontend"].deployment_group_name
        AppSpecTemplateArtifact        = "FrontendBuildOutput"
        AppSpecTemplatePath            = "appspec.yml"
        TaskDefinitionTemplateArtifact = "FrontendBuildOutput"
        TaskDefinitionTemplatePath     = "taskdef.json"
        Image1ArtifactName             = "FrontendBuildOutput"
        Image1ContainerName            = "IMAGE1_NAME"
      }
    }

    action {
      name            = "DeployBackend"
      category        = "Deploy"
      owner           = "AWS"
      provider        = "CodeDeployToECS"
      input_artifacts = ["BackendBuildOutput"]
      version         = "1"
      run_order       = 1

      configuration = {
        ApplicationName                = aws_codedeploy_app.service["backend"].name
        DeploymentGroupName            = aws_codedeploy_deployment_group.service["backend"].deployment_group_name
        AppSpecTemplateArtifact        = "BackendBuildOutput"
        AppSpecTemplatePath            = "appspec.yml"
        TaskDefinitionTemplateArtifact = "BackendBuildOutput"
        TaskDefinitionTemplatePath     = "taskdef.json"
        Image1ArtifactName             = "BackendBuildOutput"
        Image1ContainerName            = "IMAGE1_NAME"
      }
    }
  }

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-pipeline"
  })

  depends_on = [aws_iam_role_policy.codepipeline]
}
