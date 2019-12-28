package cho.carbon.cfg.imbean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import cho.carbon.cfg.annotation.FunctionGroup;
import cho.carbon.fuse.fg.CheckFuncGroup;
import cho.carbon.fuse.fg.FetchFuncGroup;
import cho.carbon.fuse.fg.FirstRoundImproveFuncGroup;
import cho.carbon.fuse.fg.FuseCallBackFuncGroup;
import cho.carbon.fuse.fg.IdentityQueryFuncGroup;
import cho.carbon.fuse.fg.QueryJunctionFuncGroup;
import cho.carbon.fuse.fg.SecondRoundImproveFuncGroup;
import cho.carbon.fuse.fg.ThirdRoundImproveFuncGroup;
import cho.carbon.utils.JavaCompilerFactory;
import cho.carbon.utils.MessageDTO;

public class ImportBeanController implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

	private Environment environment;
	private DefaultListableBeanFactory beanFactory = null;
	
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		try {
			// 存放bean, 及bean 的接口信息
			Map<String, List<Class<?>>> mapTemp = new HashMap<String, List<Class<?>>>();
			
			// 获取所有 标记了FunctionGroup  注解的bean name
			String[] beanNamesForAnnotation = beanFactory.getBeanNamesForAnnotation(FunctionGroup.class);
			for (String name : beanNamesForAnnotation) {
				// 获取当前bean
				Class<? extends Object> clazz =  beanFactory.getBean(name).getClass();
				
				 FunctionGroup annotation = clazz.getAnnotation(FunctionGroup.class);
				 String modelItemCode = annotation.value();
				int level = annotation.level();
				String beanName = modelItemCode+level;
				
				// 获取当前bean 所有的接口
				Class<?>[] interfaces = clazz.getInterfaces();
				List<Class<?>> list = Arrays.asList(interfaces);
				List<Class<?>> clazzInterList = new ArrayList(list);
				if (clazzInterList.contains(ThirdRoundImproveFuncGroup.class)) {
					clazzInterList.add(SecondRoundImproveFuncGroup.class);
					clazzInterList.add(FirstRoundImproveFuncGroup.class);
				} else if (clazzInterList.contains(SecondRoundImproveFuncGroup.class)) {
					clazzInterList.add(FirstRoundImproveFuncGroup.class);
				}
				
				//建立controller clazz 对象
				Class buildController = buildController(clazz, clazzInterList, beanName.toLowerCase());
				if (buildController != null) {
					BeanDefinition controllerBean = new RootBeanDefinition(buildController);
					registry.registerBeanDefinition(buildController.getName(), controllerBean);
				}
				
				mapTemp.put(beanName, clazzInterList);
			}
			
			MessageDTO message = new MessageDTO();
			message.setMapInter(mapTemp);
			message.setApplicationName(environment.getProperty("spring.application.name"));
			beanFactory.registerSingleton("message", message);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory)beanFactory;
	}
	
	// 动态生成restController 接口
		private  Class buildController(Class<? extends Object> clazz, List<Class<?>> clazzInterList, String sufixPath) throws Exception  {
			// 获取类的简单名称
			String clazzName = clazz.getSimpleName();
			sufixPath = sufixPath.toLowerCase();
			String clazzNameLower = clazzName.toLowerCase();
			// 构件controller 名称
			String controllerName = clazzName + "Controller";
			String rt = "\r\n";
			StringBuffer sb = new StringBuffer();
			
			String packageName = "cho.carbon.cfg.controller";
			sb.append("package  "+packageName+";" +rt)
			.append("import org.springframework.web.bind.annotation.RequestMapping;" +rt)
			.append("import org.springframework.web.bind.annotation.RestController;" +rt)
			.append("import org.springframework.beans.factory.annotation.Autowired;" +rt)
			.append("import org.springframework.web.bind.annotation.RequestMethod;" +rt)
			.append("import org.springframework.web.bind.annotation.RequestParam;" +rt)
			.append("import java.lang.String;" +rt)
			.append("import java.lang.Integer;" +rt)
			.append("import java.lang.Integer;" +rt)
			.append("import cho.carbon.dto.CarbonParam;" +rt)
			.append("import org.springframework.web.bind.annotation.RequestBody;"  +rt)
			.append("import cho.carbon.complexus.FGRecordComplexus;"  +rt)
			.append("import cho.carbon.fuse.fg.FGOSerializableFactory;"  +rt)
			.append("import java.util.Collection;"  +rt)
			.append("import cho.carbon.meta.criteria.model.ModelCriterion;"  +rt)
			.append("import cho.carbon.fuse.fg.FGOSerializableFactory;"  +rt)
			.append("import cho.carbon.complexus.FGRecordComplexus;"  +rt)
			.append("import cho.carbon.context.fg.FuncGroupContext;"  +rt)
			.append("import cho.carbon.fuse.fg.CheckFGResult;"  +rt)
			.append("import cho.carbon.fuse.fg.ImproveFGResult;"  +rt)
			.append("import cho.carbon.rrc.record.FGRootRecord;"  +rt)
			.append("import cho.carbon.fuse.fg.FetchFGResult;"  +rt)
			.append("import cho.carbon.meta.criteria.model.ModelConJunction;"  +rt)
			.append("import cho.carbon.fuse.fg.ConJunctionFGResult;"  +rt)
			.append("import cho.carbon.ops.complexus.OpsComplexus;" + rt)
			
			.append("@RestController" +rt)
			.append("public class "+controllerName+" {" +rt)
			.append("@Autowired"+rt)
			.append(clazz.getName() + " " +clazzNameLower +";"+rt);
			
			// 遍历所有接口
			for (Class<?> interClazz : clazzInterList) {
				
				// 根据接口生成controller 方法
				if (IdentityQueryFuncGroup.class.equals(interClazz)) {
					//runningAfterCodeQuery 方法
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/runningAfterCodeQuery\")" +rt)
					.append(" public boolean runningAfterCodeQuery() {" +rt)
					.append(" return "+clazzNameLower+".runningAfterCodeQuery();" +rt)
					.append(" }" +rt);
					
					//getCriterions
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/getCriterions\")" +rt)
					.append(" public String getCriterions(@RequestBody CarbonParam carbonParam) {" +rt)
					.append(" FGRecordComplexus remoteFGRecordComplexus = FGOSerializableFactory.des2FGRecordComplexus(carbonParam.getfGRecordComplexus());" +rt)
					.append(" Collection<ModelCriterion> criterions = "+clazzNameLower+".getCriterions(carbonParam.getRecordCode(), remoteFGRecordComplexus);" +rt)
					.append(" return FGOSerializableFactory.serializeCriterions(criterions);")
					.append(" }");
				} else if (CheckFuncGroup.class.equals(interClazz)) {
					// afterCheck
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/afterCheck\")" +rt)
					.append(" public String afterCheck(@RequestBody CarbonParam carbonParam) {" +rt)
					.append(" FGRecordComplexus fGRecordComplexus = FGOSerializableFactory.des2FGRecordComplexus(carbonParam.getfGRecordComplexus());"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" CheckFGResult afterCheck = "+clazzNameLower+".afterCheck(funcGroupContext, carbonParam.getRecordCode(), fGRecordComplexus);"+rt)
					.append(" return afterCheck.serialize();"+rt)
					.append(" }"+rt);
				} else if (ThirdRoundImproveFuncGroup.class.equals(interClazz)) {
					// thirdImprove
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/thirdImprove\")"+rt)
					.append(" public String thirdImprove(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" FGRecordComplexus fGRecordComplexus = FGOSerializableFactory.des2FGRecordComplexus(carbonParam.getfGRecordComplexus());"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" ImproveFGResult thirdImprove = "+clazzNameLower+".thirdImprove(funcGroupContext, carbonParam.getRecordCode(), fGRecordComplexus);"+rt)
					.append("  return thirdImprove.serialize();"+rt)
					.append(" }"+rt);
				} else if (FuseCallBackFuncGroup.class.equals(interClazz)) {
					//afterFusition
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/afterFusition\")"+rt)
					.append(" public boolean afterFusition(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" boolean afterFusition = "+clazzNameLower+".afterFusition(funcGroupContext, carbonParam.getRecordCode());"+rt)
					.append(" return afterFusition;"+rt)
					.append(" }"+rt);
				} else if (FetchFuncGroup.class.equals(interClazz)) {
					// fetchImprove
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/fetchImprove\")"+rt)
					.append(" public String fetchImprove(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" FGRootRecord fgRootRecord = FGOSerializableFactory.des2FGRootRecord(carbonParam.getfGRootRecord());"+rt)
					.append(" FetchFGResult fetchImprove = "+clazzNameLower+".fetchImprove(funcGroupContext, fgRootRecord);"+rt)
					.append(" return fetchImprove.serialize();"+rt)
					.append(" }"+rt);
				}else if (QueryJunctionFuncGroup.class.equals(interClazz)) {
					//junctionImprove
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/junctionImprove\")"+rt)
					.append(" public String junctionImprove(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" ModelConJunction modelConJunction = FGOSerializableFactory.des2ModelConJunction(carbonParam.getModelConJunction());"+rt)
					.append(" ConJunctionFGResult junctionImprove = "+clazzNameLower+".junctionImprove(funcGroupContext, modelConJunction);"+rt)
					.append(" return junctionImprove.serialize();"+rt)
					.append(" }"+rt);
				}else if (SecondRoundImproveFuncGroup.class.equals(interClazz)) {
					//secondImprove
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/secondImprove\")"+rt)
					.append(" public String secondImprove(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" FGRecordComplexus fGRecordComplexus = FGOSerializableFactory.des2FGRecordComplexus(carbonParam.getfGRecordComplexus());"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" ImproveFGResult secondImprove = "+clazzNameLower+".secondImprove(funcGroupContext, carbonParam.getRecordCode(), fGRecordComplexus);"+rt)
					.append(" return secondImprove.serialize();"+rt)
					.append(" }"+rt);
					
				}else if (FirstRoundImproveFuncGroup.class.equals(interClazz)) {
					//preImprove
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/preImprove\")"+rt)
					.append(" public String preImprove(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" FGRecordComplexus fGRecordComplexus = FGOSerializableFactory.des2FGRecordComplexus(carbonParam.getfGRecordComplexus());"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" OpsComplexus opsComplexus = FGOSerializableFactory.des2OpsComplexus(carbonParam.getOpsComplexus());"+rt)
					.append(" ImproveFGResult preImprove = "+clazzNameLower+".preImprove(funcGroupContext, carbonParam.getRecordCode(), opsComplexus, fGRecordComplexus);"+rt)
					.append(" return preImprove.serialize();"+rt)
					.append(" }"+rt);
					
					// improve
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/improve\")"+rt)
					.append(" public String improve(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" FGRecordComplexus fGRecordComplexus = FGOSerializableFactory.des2FGRecordComplexus(carbonParam.getfGRecordComplexus());"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" ImproveFGResult improve = "+clazzNameLower+".improve(funcGroupContext, carbonParam.getRecordCode(), fGRecordComplexus);"+rt)
					.append(" return improve.serialize();"+rt)
					.append(" }"+rt);
					
					// postImprove
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/postImprove\")"+rt)
					.append(" public String postImprove(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" FGRecordComplexus fGRecordComplexus = FGOSerializableFactory.des2FGRecordComplexus(carbonParam.getfGRecordComplexus());"+rt)
					.append(" FuncGroupContext funcGroupContext = FGOSerializableFactory.des2FuncGroupContext(carbonParam.getFuncGroupContext());"+rt)
					.append(" ImproveFGResult postImprove = "+clazzNameLower+".postImprove(funcGroupContext, carbonParam.getRecordCode(), fGRecordComplexus);"+rt)
					.append(" return postImprove.serialize();"+rt)
					.append(" }"+rt);
					
					// improveOnlyCorrelativeRelation
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/improveOnlyCorrelativeRelation\")"+rt)
					.append(" public boolean improveOnlyCorrelativeRelation() {"+rt)
					.append(" boolean improveOnlyCorrelativeRelation = "+clazzNameLower+".improveOnlyCorrelativeRelation();"+rt)
					.append(" return improveOnlyCorrelativeRelation;"+rt)
					.append(" }"+rt);
					
					// improveEveryTime
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/improveEveryTime\")"+rt)
					.append(" public boolean improveEveryTime() {"+rt)
					.append(" 	boolean improveEveryTime = "+clazzNameLower+".improveEveryTime();"+rt)
					.append("  return improveEveryTime;"+rt)
					.append(" }"+rt);
					// needImprove
					sb.append(" @RequestMapping(value = \"/"+sufixPath+"/needImprove\")"+rt)
					.append(" public boolean needImprove(@RequestBody CarbonParam carbonParam) {"+rt)
					.append(" OpsComplexus opsComplexus = FGOSerializableFactory.des2OpsComplexus(carbonParam.getOpsComplexus());"+rt)
					.append(" boolean needImprove = "+clazzNameLower+".needImprove(carbonParam.getRecordCode(), opsComplexus);"+rt)
					.append(" return needImprove;"+rt)
					.append(" }"+rt);
				}
				
			}
			// 类结束标志
			sb.append("}" +rt);
			
			return JavaCompilerFactory.compilerJavaFile(packageName, controllerName, sb.toString().getBytes(), true, true);
		}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

}
