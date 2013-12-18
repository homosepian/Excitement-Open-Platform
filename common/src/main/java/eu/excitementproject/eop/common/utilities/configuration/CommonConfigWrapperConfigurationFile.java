package eu.excitementproject.eop.common.utilities.configuration;

import java.io.File;

import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.configuration.NameValueTable;

/**
 * 
 * @author Asher Stern
 * @since Dec 18, 2013
 *
 */
public class CommonConfigWrapperConfigurationFile implements UnderlyingConfigurationFile
{
	private static final long serialVersionUID = 1632860238502900978L;
	
	public CommonConfigWrapperConfigurationFile(CommonConfig commonConfig, ConfigurationFile configurationFileReference)
	{
		this.commonConfig = commonConfig;
		this.configurationFileReference = configurationFileReference;
	}

	@Override
	public boolean isExpandingEnvironmentVariables()
	{
		return expandingEnvironmentVariables;
	}

	@Override
	public void setExpandingEnvironmentVariables(boolean expandingEnvironmentVariables)
	{
		this.expandingEnvironmentVariables = expandingEnvironmentVariables;
	}

	@Override
	public ConfigurationParams getParams()
	{
		return null;
	}

	@Override
	public ConfigurationParams getModuleConfiguration(String iModuleName) throws ConfigurationException
	{
		try
		{
			NameValueTable table = commonConfig.getSection(iModuleName);
			return new ExcitementConfigurationParams(commonConfig, table, iModuleName, expandingEnvironmentVariables, configurationFileReference);
		}
		catch (eu.excitementproject.eop.common.exception.ConfigurationException e)
		{
			throw new ConfigurationException("Failed to get module "+iModuleName+". See nested exception.",e);
		}
	}

	@Override
	public boolean isModuleExist(String moduleName) throws ConfigurationException
	{
		try
		{
			commonConfig.getSection(moduleName);
			return true;
		}
		catch(eu.excitementproject.eop.common.exception.ConfigurationException e)
		{
			return false;
		}
	}

	@Override
	public void addModuleConfiguration(String iModuleName) throws ConfigurationException
	{
		throw new ConfigurationException("Operation addModuleConfiguration is not supported in this implementation ("+this.getClass().getSimpleName()+").");
	}

	@Override
	public void removeModuleConfiguration(String iModuleName) throws ConfigurationException
	{
		throw new ConfigurationException("Operation removeModuleConfiguration is not supported in this implementation ("+this.getClass().getSimpleName()+").");
	}

	@Override
	public File getConfFile()
	{
		return new File(commonConfig.getConfigurationFileName());
	}

	protected final CommonConfig commonConfig;
	protected final ConfigurationFile configurationFileReference;
	protected boolean expandingEnvironmentVariables = false;
}
