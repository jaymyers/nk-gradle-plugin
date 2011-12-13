/*
 * Copyright 2011 Brian Sletten
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.bosatsu.gradle

import org.gradle.api.Project

class NetKernelConvention {
	File netKernelRootDir
	Project p
	
	static String LOCAL_INSTALLATION_URL = "http://localhost:1060/tools/scriptplaypen?action2=execute&type=gy&example&identifier&name&space&script=context.createResponseFrom%28context.source%28%22netkernel:/config/netkernel.install.path%22%29%29"

	def modules
	def relatedProjects = []

	def packages = []
	
	NetKernelConvention(Project p) {
		this.p = p
		
		def overridden = false
		def location
		
		if(p.hasProperty('netkernelroot')) {
			location = p.netkernelroot
		}
		
		if(location == null) {
			location = checkForRunningSystem(p)
			// TODO: Write out to .gradle/gradle.properties?
		}
		
		if(System.properties.netkernelroot) {
			overridden = netKernelRootDir != null
			location = System.properties.netkernelroot
		}
		
		if(location != null) {
			netKernelRootDir = p.file(location)

			if(netKernelRootDir.exists()) {
				if(overridden) {
					println "Overriding NetKernel installation to: ${location}"
				} else {
					println "Found a NetKernel installation at: ${location}"				
				}
			
			} else {
				println "NetKernel Gradle plugin currently requires you to specify a NetKernel installation directory."
				println 'Please put a gradle.properties file in user.home/.gradle or use: gradle -Dnetkernelhome=<installation>'
			}
		}
	}
	
	def checkForRunningSystem(Project p) {
		def retValue = null
		
		try {
		   def u = new URL(LOCAL_INSTALLATION_URL)
		   def installation = u.getText()
		
		   if(p.file(installation).exists()) {
			  if(installation.startsWith("file:")) {
				installation = installation.substring(4)
			  }
		      println "Discovered NetKernel installation: $installation"
		      retValue = installation
		   } else {
		  	  println "Ignoring non-existent installation: $installation"
		   }

		} catch(Throwable t) {
			//t.printStackTrace()
			println t.getMessage()
		}
		
		retValue
	}
	
	def dependsOnNetkernelModule(String moduleName) {
		Project otherProject = p.project(moduleName)

		p.repositories {
			flatDir(name: "${otherProject.projectDir}-lib", dirs: ["${otherProject.projectDir}/lib"])
		}
		
		p.dependencies {
	        compile otherProject
		}
	}
	
	def nkconfig(Closure closure) {
		closure.delegate = this
		closure()
	}
	
	def definePackage(Map map) {
		if(map['name'] != null && map['description'] != null && map['version'] != null) {
			packages << map		
		} else {
			println "WARNING: Specified package: $map must include 'name', 'description' and 'version' attributes."
		}
	}
}